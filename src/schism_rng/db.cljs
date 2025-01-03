(ns schism-rng.db)

(def default-db
  (let
   [old ["Anansi" "Atete" "Dziva" "Elegua" "Engai" "Eshu" "Gu" "Obatala" "Qamata" "Shadipinyi" "Tsui-Goab" "Xango"]
    new ["Agwu-Nsi" "Ala" "Alajire" "Anyanwu" "Igwekala" "Orisha Aje" "Tiurakh" "Ogun" "Oya" "Ekwensu" "Ovia" "Yemoja" "Aje Shaluga" "Ajaka" "Olokun"]
    gods (vec
          (concat
           (mapv (fn [a]
                   [a :old])
                 (sort old))
           (mapv (fn [a]
                   [a :new])
                 (sort new))))]
    {:gods gods
     :state (mapv (fn [[a _]] [false (= a "Dziva")]) gods)
     :god-count "8"
     :min-old "4"
     :min-new "4"
     :result []}))
