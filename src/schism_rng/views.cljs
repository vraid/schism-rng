(ns schism-rng.views
  (:require
   [re-frame.core :as re-frame]
   [schism-rng.subs :as subs]
   [schism-rng.events :as events]
   [clojure.string :as string]))

(defn gettext [e] (-> e .-target .-value))

(defn getchecked [e] (-> e .-target .-checked))

(def checkbox-style {:width "20px" :height "20px"})

(defn main-panel []
  (let
   [data @(re-frame/subscribe [::subs/db])
    gods (:gods data)
    state (:state data)
    update-input (fn [key]
                   (fn [e] (re-frame/dispatch
                            [::events/set-value key (gettext e)])))
    input (fn [key]
            [:input {:style {:width "40px"}
                     :type "text"
                     :value (get data key)
                     :on-change (update-input key)}])
    input-row (fn [key label]
                [:div {:style {:display :flex
                               :flex-wrap :wrap}}
                 [:div {:style {:min-width "100px"}} label]
                 (input key)])
    include (fn [n value]
              (re-frame/dispatch
               [::events/set-value :state (assoc state n [(getchecked value) false])]))
    exclude (fn [n value]
              (re-frame/dispatch
               [::events/set-value :state (assoc state n [false (getchecked value)])]))
    generate (fn [_]
               (let
                [parse (fn [a] (max 0 (or (parse-long a) 0)))
                 god-count (parse (:god-count data))
                 old-min (parse (:min-old data))
                 new-min (parse (:min-new data))]
                 (if (> (+ old-min new-min) god-count)
                   (re-frame/dispatch
                    [::events/set-value :result []])
                   (let
                    [stateful-gods (mapv (fn [a b] [a b]) gods state)
                     taken (mapv (fn [[a _]] a) (filter (fn [[_ [always _]]] always) stateful-gods))
                     [old-taken new-taken] (mapv (fn [type] (count (filter (fn [[_ a]] (= a type)) taken))) [:old :new])
                     old-min (max 0 (- old-min old-taken))
                     new-min (max 0 (- new-min new-taken))]
                     (if (> (+ old-taken new-taken old-min new-min) god-count)
                       (re-frame/dispatch
                        [::events/set-value :result []])
                       (let
                        [gods (shuffle (mapv (fn [[a _]] a) (filter (fn [[_ [always never]]] (and (not always) (not never))) stateful-gods)))
                         [old new] (mapv (fn [type] (filter (fn [[_ a]] (= a type)) gods)) [:old :new])
                         taken (concat taken (take old-min old) (take new-min new))
                         remaining (concat (drop old-min old) (drop new-min new))
                         taken (concat taken (take (- god-count (count taken)) (shuffle remaining)))]
                         (re-frame/dispatch
                          [::events/set-value :result (mapv first taken)])))))))]
    (vec
     (concat
      [:div
       [:h1
        "Super Secret RNG"]]
      [[:div (str (:shit data))]]
      [[:div {:style {:display :flex
                      :flex-wrap :wrap}}
        [:div {:style {:min-width "100px"}} [:b "God"]]
        [:div {:style {:min-width "100px"}} [:b "Include"]]
        [:div {:style {:min-width "100px"}} [:b "Exclude"]]]
       [:h3]]
      (mapv (fn [n]
              (let
               [[name _] (nth gods n)
                [always never] (nth state n)]
                [:div {:style {:display :flex
                               :flex-wrap :wrap}}
                 [:div {:style {:min-width "100px"}} [:label name]]
                 [:div {:style {:min-width "100px"}} [:input {:style checkbox-style :type :checkbox :checked always :on-change (partial include n)}]]
                 [:div {:style {:min-width "100px"}} [:input {:style checkbox-style :type :checkbox :checked never :on-change (partial exclude n)}]]]))
            (range (count gods)))
      [[:h3] [:div {:style {:border-bottom "1px solid black"
                            :width "250px"}}]
       [:h3]]
      [(input-row :god-count "Total gods")
       (input-row :min-old "Old, at least")
       (input-row :min-new "New, at least")
       [:button
        {:style {:padding "8px 8px"
                 :margin "4px 4px"}
         :on-click generate}
        "Go!"]]
      [[:div (if (empty? (:result data))
               [:h3]
               "The lucky few are..")]]
      [[:div [:textarea
              {:cols 30
               :rows 12
               :read-only true
               :value (string/join "\n" (:result data))}]]]))))
