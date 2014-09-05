(ns trumpet-server.domain.trumpeteer-trumpet-test
  (:require [midje.sweet :refer :all]
            [trumpet-server.domain.trumpeteer :refer :all]))

(fact "Send trumpet to all trumpeteers execept self and those that are out-of-range"
      ; Given
      (def trumpeteer1 (->Trumpeteer 1, 55.583985, 12.957578)) ; self
      (def trumpeteer2 (->Trumpeteer 2, 55.584126, 12.957406))
      (def trumpeteer3 (->Trumpeteer 3, 55.58453, 12.957406))
      (def trumpeteer4 (->Trumpeteer 4, 55.58533, 12.95864))
      (def trumpeteer5 (->Trumpeteer 5, 55.59772, 12.97519)) ; out of range
      (def trumpeteer6 (->Trumpeteer 6, 55.60519, 13.00334)) ; out of range

      (def messages-sent (atom {}))

      ; When
      (trumpet! trumpeteer1 {:trumpetees   [trumpeteer1 trumpeteer2 trumpeteer3 trumpeteer4 trumpeteer5 trumpeteer6]
                             :broadcast-fn (fn [id message] (swap! messages-sent assoc id message))
                             :trumpet      "Hello World!"})

      ; Then
      (keys (deref messages-sent)) => (just (list 2 3 4) :in-any-order))

(fact "Generated trumpet message contains the trumpet and distance from source"
      ; Given
      (def trumpeteer1 (->Trumpeteer 1, 55.583985, 12.957578)) ; self
      (def trumpeteer2 (->Trumpeteer 2, 55.584126, 12.957406))
      (def trumpeteer3 (->Trumpeteer 3, 55.58453, 12.957406))
      (def trumpeteer4 (->Trumpeteer 4, 55.58533, 12.95864))
      (def trumpeteer5 (->Trumpeteer 5, 55.59772, 12.97519)) ; out of range
      (def trumpeteer6 (->Trumpeteer 6, 55.60519, 13.00334)) ; out of range

      (def messages-sent (atom {}))

      ; When
      (trumpet! trumpeteer1 {:trumpetees   [trumpeteer1 trumpeteer2 trumpeteer3 trumpeteer4 trumpeteer5 trumpeteer6]
                             :broadcast-fn (fn [id message] (swap! messages-sent assoc id message))
                             :trumpet      "Hello World!"})

      ; Then
      (def messages (vals (deref messages-sent)))
      (map #(:message %) messages) => (has every? #(= % "Hello World!"))
      (map #(:distanceFromSource %) messages) => (has every? #(> % 0)))

(fact "Send trumpet to all trumpeteers execept self and those that are out-of-range when explicity specifying max distance"
      ; Given
      (def trumpeteer1 (->Trumpeteer 1, 55.583985, 12.957578)) ; self
      (def trumpeteer2 (->Trumpeteer 2, 55.584126, 12.957406))
      (def trumpeteer3 (->Trumpeteer 3, 55.58453, 12.957406))
      (def trumpeteer4 (->Trumpeteer 4, 55.58533, 12.95864))
      (def trumpeteer5 (->Trumpeteer 5, 55.59772, 12.97519)) ; out of range
      (def trumpeteer6 (->Trumpeteer 6, 55.60519, 13.00334)) ; out of range

      (def messages-sent (atom {}))

      ; When
      (trumpet! trumpeteer1 {:trumpetees          [trumpeteer1 trumpeteer2 trumpeteer3 trumpeteer4 trumpeteer5 trumpeteer6]
                             :broadcast-fn        (fn [id message] (swap! messages-sent assoc id message))
                             :trumpet             "Hello World!"
                             :max-distance-meters 150})
      ; Then
      (keys (deref messages-sent)) => (just (list 2 3) :in-any-order))

(fact "uses 200 meters as distance when max-distance-in-meters is explicitly nil"
      ; Given
      (def trumpeteer1 (->Trumpeteer 1, 55.583985, 12.957578)) ; self
      (def trumpeteer2 (->Trumpeteer 2, 55.584126, 12.957406))
      (def trumpeteer3 (->Trumpeteer 3, 55.58453, 12.957406))
      (def trumpeteer4 (->Trumpeteer 4, 55.58533, 12.95864))
      (def trumpeteer5 (->Trumpeteer 5, 55.59772, 12.97519)) ; out of range
      (def trumpeteer6 (->Trumpeteer 6, 55.60519, 13.00334)) ; out of range

      (def messages-sent (atom {}))

      ; When
      (trumpet! trumpeteer1 {:trumpetees          [trumpeteer1 trumpeteer2 trumpeteer3 trumpeteer4 trumpeteer5 trumpeteer6]
                             :broadcast-fn        (fn [id message] (swap! messages-sent assoc id message))
                             :trumpet             "Hello World!"
                             :max-distance-meters nil})
      ; Then
      (keys (deref messages-sent)) => (just (list 2 3 4) :in-any-order))


(fact "uses 200 meters as distance when max-distance-in-meters is greater than 200"
      ; Given
      (def trumpeteer1 (->Trumpeteer 1, 55.583985, 12.957578)) ; self
      (def trumpeteer2 (->Trumpeteer 2, 55.584126, 12.957406))
      (def trumpeteer3 (->Trumpeteer 3, 55.58453, 12.957406))
      (def trumpeteer4 (->Trumpeteer 4, 55.58533, 12.95864))
      (def trumpeteer5 (->Trumpeteer 5, 55.59772, 12.97519)) ; out of range
      (def trumpeteer6 (->Trumpeteer 6, 55.60519, 13.00334)) ; out of range

      (def messages-sent (atom {}))

      ; When
      (trumpet! trumpeteer1 {:trumpetees          [trumpeteer1 trumpeteer2 trumpeteer3 trumpeteer4 trumpeteer5 trumpeteer6]
                             :broadcast-fn        (fn [id message] (swap! messages-sent assoc id message))
                             :trumpet             "Hello World!"
                             :max-distance-meters 100000})
      ; Then
      (keys (deref messages-sent)) => (just (list 2 3 4) :in-any-order))

