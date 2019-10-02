(ns comment-api-server.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [io.pedestal.interceptor :as interceptor]
            [cheshire.core :as json]
            [io.pedestal.log :as log]
            [data.comments :as comments]
            [data.db :as db]
            [clojure.walk :as walk]))

(def content-length-json-body
  (interceptor/interceptor
   {:name ::content-length-json-body
    :leave (fn [context]
             (let [response (:response context)
                   body (:body response)
                   json-response-body (if body (json/generate-string body) "")
                    ;; Content-Length is the size of the response in bytes
                    ;; Let's count the bytes instead of the string, in case there are unicode characters
                   content-length (count (.getBytes ^String json-response-body))
                   headers (:headers response {})]
               (assoc context
                      :response {:status (:status response)
                                 :body json-response-body
                                 :headers (merge headers
                                                 {"Content-Type" "application/json;charset=UTF-8"
                                                  "Content-Length" (str content-length)})})))}))

(defn exists-db
  [path]
  (.exists (clojure.java.io/as-file path)))

(defn create-db-if-not-exists
  []
  (if (not (exists-db (:subname db/db-spec)))
    (comments/create-comments-table db/db-spec)))

(defn db-comments
  [page-id]
  (create-db-if-not-exists)
  (let [cs (seq (comments/comments-by-page db/db-spec {:page_id page-id}))]
    (if cs cs '())))

(defn get-comments
  [request]
  (let [page-id (:page-id (:path-params request))
        comments (db-comments page-id)]
    (ring-resp/response comments)))

(defn db-new-comment
  [page-id ip params]
  (create-db-if-not-exists)
  (let [p (assoc (walk/keywordize-keys params) :page_id page-id :ip ip)]
    (comments/insert-comment db/db-spec p)))

(defn add-comment
  [request]
  (let [page-id (:page-id (:path-params request))
        result (db-new-comment page-id (:remote-addr request) (:params request))]
    (ring-resp/response {:success result})))

(def common-interceptors [(body-params/body-params) http/json-body])
(def custom-interceptors [(body-params/body-params) content-length-json-body])

(def routes #{["/comment/:page-id" :get (conj common-interceptors `get-comments) :constraints {:page-id #"^[a-zA-Z0-9\-_/\%]+"}]
              ["/comment/:page-id" :post (conj common-interceptors `add-comment) :constraints {:page-id #"^[a-zA-Z0-9\-_/\%]+"}]})

;; Consumed by comment-api-server.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ::http/allowed-origins {:creds true :allowed-origins ["https://www.sysbe.net"]}

              ;; Tune the Secure Headers
              ;; and specifically the Content Security Policy appropriate to your service/application
              ;; For more information, see: https://content-security-policy.com/
              ;;   See also: https://github.com/pedestal/pedestal/issues/499
              ;;::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
              ;;                                                          :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
              ;;                                                          :frame-ancestors "'none'"}}

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ;;  This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
              ::http/type :jetty
              ;;::http/host "localhost"
              ::http/port 8080
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false
                                        ;; Alternatively, You can specify you're own Jetty HTTPConfiguration
                                        ;; via the `:io.pedestal.http.jetty/http-configuration` container option.
                                        ;:io.pedestal.http.jetty/http-configuration (org.eclipse.jetty.server.HttpConfiguration.)
                                        }})
