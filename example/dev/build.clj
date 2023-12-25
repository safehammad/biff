(ns build)

(defn hello
  "Says 'Hello'"
  [_]
  (println "Hello"))

(def tasks {"hello" #'hello})

(defn -main [& args]
  (compile 'com.biffweb.build)
  (compile 'clojure.edn)
  ((requiring-resolve 'com.biffweb.build/dispatch)
   (merge {:biff.tasks/tasks (merge @(requiring-resolve 'com.biffweb.build/tasks)
                                    tasks)
           :biff.tasks/main-ns 'com.example}
          ;; TODO don't fail when file not present
          (:tasks ((requiring-resolve 'clojure.edn/read-string) "config.edn")))
   args))
