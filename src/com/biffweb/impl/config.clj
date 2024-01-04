(ns com.biffweb.impl.config
  (:require [aero.core :as aero]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [com.biffweb.impl.util :as util]
            [com.biffweb.impl.util.ns :as util-ns]))

(defn keyword->var-name [k]
  (-> k
      str
      (str/replace #"^:" "")
      (str/replace #"[\./-]" "_")
      str/upper-case))

(defn env->config [env config-spec]
  (into {}
        (map (fn [[k {:keys [default coerce var-name secret]
                      :or {coerce identity}}]]
               (let [var-name (or var-name (keyword->var-name k))
                     value (or (some-> (get env var-name) not-empty coerce)
                               default)]
                 [k (if secret
                      (fn [] value)
                      value)])))
        config-spec))

;; Algorithm adapted from dotenv-java:
;; https://github.com/cdimascio/dotenv-java/blob/master/src/main/java/io/github/cdimascio/dotenv/internal/DotenvParser.java
;; Wouldn't hurt to take a more thorough look at Ruby dotenv's algorithm:
;; https://github.com/bkeepers/dotenv/blob/master/lib/dotenv/parser.rb
(defn parse-env-var [line]
  (let [line (str/trim line)
        [_ _ k v] (re-matches #"^\s*(export\s+)?([\w.\-]+)\s*=\s*(['][^']*[']|[\"][^\"]*[\"]|[^#]*)?\s*(#.*)?$"
                              line)]
    (when-not (or (str/starts-with? line "#")
                  (str/starts-with? line "////")
                  (empty? v))
      (let [v (str/trim v)
            v (if (or (re-matches #"^\".*\"$" v)
                      (re-matches #"^'.*'$" v))
                (subs v 1 (dec (count v)))
                v)]
        [k v]))))

(defn get-config [config-spec]
  (let [env-file (System/getProperty "biff.env.file")
        env (merge (some->> (util/catchall (slurp env-file))
                            str/split-lines
                            (keep parse-env-var)
                            (into {}))
                   (into {} (System/getenv)))]
    (env->config env config-spec)))

(defn use-env-config [{:keys [biff.config/spec] :as ctx}]
  (let [{:keys [biff.middleware/cookie-secret
                biff/jwt-secret]
         :as ctx} (merge ctx (get-config spec))]
    (when-not (or (cookie-secret) (jwt-secret))
      (binding [*out* *err*]
        (println "Secrets are missing. You may need to run `clj -Mdev generate-secrets` "
                 "and then either edit env/dev.env or set them via environment variables.")
        (System/exit 1)))
    (doseq [[k v] (util-ns/select-ns-as ctx 'biff.system-properties nil)]
      (System/setProperty (name k) v))
    ctx))

(defmethod aero/reader 'biff/env
  [{:keys [profile biff.aero/env] :as opts} _ value]
  (not-empty (get env (str value))))

(defmethod aero/reader 'biff/secret
  [{:keys [profile biff.aero/env] :as opts} _ value]
  (when-some [value (aero/reader opts 'biff/env value)]
    (fn [] value)))

(defn use-aero-config [ctx]
  (let [profile (keyword (System/getProperty "biff.profile"))
        env (merge (some->> (util/catchall (slurp "config.env"))
                            str/split-lines
                            (keep parse-env-var)
                            (into {}))
                   (into {} (System/getenv)))
        ctx (merge ctx (aero/read-config (io/resource "config.edn") {:profile profile :biff.aero/env env}))
        secret-fn (fn get-secret
                    ([k] (some-> (get ctx k) (.invoke)))
                    ;; Backwards compatibility
                    ([ctx k] (get-secret k)))
        ctx (assoc ctx :biff/secret secret-fn)]
    (when-not (and (secret-fn :biff.middleware/cookie-secret)
                   (secret-fn :biff/jwt-secret))
      (binding [*out* *err*]
        (println "Secrets are missing. You may need to run `clj -Mdev generate-secrets` "
                 "and then either edit config.env or set them via environment variables.")
        (System/exit 1)))
    (doseq [[k v] (util-ns/select-ns-as ctx 'biff.system-properties nil)]
      (System/setProperty (name k) v))
    ctx))

;;;; Deprecated

(defn read-config [path]
  (let [env (keyword (or (System/getenv "BIFF_ENV") "prod"))
        env->config (edn/read-string (slurp path))
        config-keys (concat (get-in env->config [env :merge]) [env])
        config (apply merge (map env->config config-keys))]
    config))
