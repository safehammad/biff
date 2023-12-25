(ns com.example.config
  (:require [com.biffweb :as biff]
            [clojure.string :as str]))

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
                     value (if (contains? env var-name)
                             (some-> (get env var-name) not-empty coerce)
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
        env (merge (into {} (System/getenv))
                   (some->> (biff/catchall (slurp env-file))
                            str/split-lines
                            (keep parse-env-var)
                            (into {})))]
    (env->config env config-spec)))

(defn use-config [{:biff/keys [config-spec system-properties] :as ctx}]
  (let [config (get-config config-spec)
        secret-fn (fn [k]
                    (let [secret (get config k)]
                      (when (fn? secret)
                        (secret))))]
    (when-not (some secret-fn [:biff.middleware/cookie-secret :biff/jwt-secret])
      (binding [*out* *err*]
        ;; TODO autogenerate env files if they're missing?
        (println "Secrets are missing. You may need to run `clj -Mdev generate-secrets`"
                 "and edit env/dev.env.")
        #_(System/exit 1)))
    (doseq [[k v] system-properties]
      (System/setProperty k v))
    (merge ctx config {:biff/secret secret-fn})))

;; =============================================================================

(def system-properties
  {"user.timezone" "UTC"
   "clojure.tools.logging.factory" "clojure.tools.logging.impl/slf4j-factory"})

(def config-spec
  {:biff.beholder/enabled         {:default false, :coerce parse-boolean}
   :biff.middleware/cookie-secret {:secret true}
   :biff.middleware/secure        {:default true, :coerce parse-boolean}
   :biff.xtdb.jdbc/jdbcUrl        {:secret true}
   :biff.xtdb/dir                 {:default "storage/xtdb"}
   :biff.xtdb/topology            {:default :standalone, :coerce keyword}
   :biff/base-url                 {:default "http://localhost:8080"}
   :biff/host                     {:default "localhost", :var-name "HOST"}
   :biff/jwt-secret               {:secret true}
   :biff/port                     {:default 8080, :coerce parse-long, :var-name "PORT"}
   :postmark/api-key              {:secret true}
   :postmark/from                 {}
   :recaptcha/secret-key          {:secret true}
   :recaptcha/site-key            {}})
