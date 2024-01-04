(ns com.example.config
  (:require [com.biffweb :as biff]
            [clojure.string :as str]))


;; =============================================================================

(def spec
  {:biff.middleware/cookie-secret {:secret true}
   :biff.xtdb.jdbc/jdbcUrl        {:secret true}
   :biff/jwt-secret               {:secret true}
   :postmark/api-key              {:secret true}
   :recaptcha/secret-key          {:secret true}

   :biff.beholder/enabled  {:default false, :coerce parse-boolean}
   :biff.middleware/secure {:default true, :coerce parse-boolean}
   :biff.xtdb/dir          {:default "storage/xtdb"}
   :biff.xtdb/topology     {:default :standalone, :coerce keyword}
   :biff/base-url          {:default "http://localhost:8080"}
   :biff/host              {:default "localhost", :var-name "HOST"}
   :biff/port              {:default 8080, :coerce parse-long, :var-name "PORT"}
   :postmark/from          {}
   :recaptcha/site-key     {}

   :biff.system-properties/user.timezone                 {:default "UTC"}
   :biff.system-properties/clojure.tools.logging.factory {:default "clojure.tools.logging.impl/slf4j-factory"}
   
   })


{:biff.middleware/cookie-secret {:secret true}
 :biff/port {:default 8080, :coerce parse-long, :var-name "PORT"}
 :biff.system-properties/user.timezone {:default "UTC"}
 ...

 :biff.beholder/enabled  {:default false, :coerce parse-boolean}
 :biff.middleware/secure {:default true, :coerce parse-boolean}
 :biff.xtdb/dir          {:default "storage/xtdb"}
 :biff.xtdb/topology     {:default :standalone, :coerce keyword}
 :biff/base-url          {:default "http://localhost:8080"}
 :biff/host              {:default "localhost", :var-name "HOST"}
 :postmark/from          {}
 :recaptcha/site-key     {}

 :biff.system-properties/user.timezone                 {:default "UTC"}
 :biff.system-properties/clojure.tools.logging.factory {:default "clojure.tools.logging.impl/slf4j-factory"}}
