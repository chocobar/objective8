(ns objective8.front-end.workflows.stonecutter
  (:require [org.httpkit.client :as http]
            [bidi.ring :refer [make-handler]]
            [ring.util.response :as response]
            [objective8.utils :as utils] 
            [cheshire.core :as json]))

(def auth-base-url "http://<stonecutter>:<stonecutter-port>")
(def client-id "JPIJJLNTNODGGXW7UHIIUOESBGTZUW2L")
(def client-secret "F6AE4ILQPOGWIKJKJS4NP43UH47ZV5JJ")

(defn stonecutter-sign-in [request]
  (let [callback-uri (str utils/host-url "/stonecutter-callback")
        oauth-authorisation-path (str auth-base-url "/authorisation?client_id=" client-id
                                      "&response_type=code&redirect_uri=" callback-uri)
        response (-> (response/redirect oauth-authorisation-path)
                     (assoc :params {:client_id client-id :response_type "code" :redirect_uri callback-uri})
                     (assoc-in [:headers "accept"] "text/html"))]
    response))

(defn oauth-callback [request]
  (when-let [auth-code (get-in request [:params :code])]
    (let [callback-uri (str utils/host-url "/stonecutter-callback")
          oauth-token-path (str auth-base-url "/api/token")
          token-response @(http/post oauth-token-path {:form-params {:grant_type    "authorization_code"
                                                                     :redirect_uri  callback-uri
                                                                     :code          auth-code
                                                                     :client_id     client-id
                                                                     :client_secret client-secret}})
          token-body (-> token-response
                         :body
                         (json/parse-string keyword))]
      (-> (response/redirect (str utils/host-url "/sign-up"))
          (assoc :session {:twitter-id (str "STONECUTTER-" (:user-id token-body))})))))

(def stonecutter-workflow
  (make-handler ["/" {"stonecutter-sign-in" :sign-in
                      "stonecutter-callback" :oauth-callback}]
                {:sign-in stonecutter-sign-in
                 :oauth-callback oauth-callback}))
