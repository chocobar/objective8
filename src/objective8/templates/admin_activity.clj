(ns objective8.templates.admin-activity  
  (:require [net.cgrand.enlive-html :as html]
            [objective8.templates.page-furniture :as pf]
            [objective8.utils :as utils]
            [objective8.templates.template-functions :as tf])) 

(def admin-activity-template (html/html-resource "templates/jade/admin-activity.html"))

(def removals-list-item-snippet (first (html/select admin-activity-template [:.clj-admin-removals-list-item])))

(defn empty-removals-list-item [{:keys [translations] :as context}]
  (html/at removals-list-item-snippet
           [:.clj-admin-removal-uri] (html/content (translations :admin-activity/no-admin-removals))
           [:.clj-admin-removal-time] nil))

(defn removals-list-items [{:keys [data] :as context}]
  (let [admin-removals (:admin-removals data)]
    (html/at removals-list-item-snippet [:.clj-admin-removals-list-item]
             (html/clone-for [admin-removal admin-removals]
                             [:.clj-admin-removal-uri] (html/content (:removal-uri admin-removal))
                             [:.clj-admin-removal-time] (html/content (utils/iso-time-string->pretty-time (:_created_at admin-removal)))))))

(defn admin-activity-page [{:keys [doc data] :as context}]
  (let [admin-removals (:admin-removals data)]
    (apply str
           (html/emit*
             (tf/translate context
                           (pf/add-google-analytics 
                             (html/at admin-activity-template
                                      [:title] (html/content (:title doc))
                                      [(and (html/has :meta) (html/attr= :name "description"))] (html/set-attr "content" (:description doc))
                                      [:.clj-masthead-signed-out] (html/substitute (pf/masthead context))
                                      [:.clj-status-bar] (html/substitute (pf/status-flash-bar context))
                                      [:.clj-admin-removals-list] (html/content (if (empty? admin-removals) 
                                                                                  (empty-removals-list-item context)
                                                                                  (removals-list-items context))))))))))
