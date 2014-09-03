(ns trumpet-server.domain.trumpeter-trumpet-test
  (:require [midje.sweet :refer :all]
            [trumpet-server.domain.trumpeter :refer :all]))

(fact "Send trumpet to all trumpeters execept self and those that are out-of-range"
      ; Given
      (def trumpeter1 (->Trumpeter 1, 55.583985, 12.957578)) ; self
      (def trumpeter2 (->Trumpeter 2, 55.584126, 12.957406))
      (def trumpeter3 (->Trumpeter 3, 55.58453, 12.957406))
      (def trumpeter4 (->Trumpeter 4, 55.58533, 12.95864))
      (def trumpeter5 (->Trumpeter 5, 55.59772, 12.97519))  ; out of range
      (def trumpeter6 (->Trumpeter 6, 55.60519, 13.00334))  ; out of range

      (def messages-sent (atom {}))

      ; When
      (trumpet trumpeter1 {:targets     [trumpeter1 trumpeter2 trumpeter3 trumpeter4 trumpeter5 trumpeter6]
                           :broadcaster (fn [id message] (swap! messages-sent assoc id message))
                           :trumpet     "Hello World!"})

      ; Then
      (keys (deref messages-sent)) => (just (list 2 3 4) :in-any-order))

(fact "Generated trumpet message contains the trumpet and distance from source"
      ; Given
      (def trumpeter1 (->Trumpeter 1, 55.583985, 12.957578)) ; self
      (def trumpeter2 (->Trumpeter 2, 55.584126, 12.957406))
      (def trumpeter3 (->Trumpeter 3, 55.58453, 12.957406))
      (def trumpeter4 (->Trumpeter 4, 55.58533, 12.95864))
      (def trumpeter5 (->Trumpeter 5, 55.59772, 12.97519))  ; out of range
      (def trumpeter6 (->Trumpeter 6, 55.60519, 13.00334))  ; out of range

      (def messages-sent (atom {}))

      ; When
      (trumpet trumpeter1 {:targets     [trumpeter1 trumpeter2 trumpeter3 trumpeter4 trumpeter5 trumpeter6]
                           :broadcaster (fn [id message] (swap! messages-sent assoc id message))
                           :trumpet     "Hello World!"})

      ; Then
      (def messages (vals (deref messages-sent)))
      (map #(:message %) messages) => (has every? #(= % "Hello World!"))
      (map #(:distanceFromSource %) messages) => (has every? #(> % 0)))

(fact "Send trumpet to all trumpeters execept self and those that are out-of-range when explicity specifying max distance"
      ; Given
      (def trumpeter1 (->Trumpeter 1, 55.583985, 12.957578)) ; self
      (def trumpeter2 (->Trumpeter 2, 55.584126, 12.957406))
      (def trumpeter3 (->Trumpeter 3, 55.58453, 12.957406))
      (def trumpeter4 (->Trumpeter 4, 55.58533, 12.95864))
      (def trumpeter5 (->Trumpeter 5, 55.59772, 12.97519))  ; out of range
      (def trumpeter6 (->Trumpeter 6, 55.60519, 13.00334))  ; out of range

      (def messages-sent (atom {}))

      ; When
      (trumpet trumpeter1 {:targets     [trumpeter1 trumpeter2 trumpeter3 trumpeter4 trumpeter5 trumpeter6]
                           :broadcaster (fn [id message] (swap! messages-sent assoc id message))
                           :trumpet     "Hello World!"
                           :max-distance-meters 150})
      ; Then
      (keys (deref messages-sent)) => (just (list 2 3) :in-any-order))
