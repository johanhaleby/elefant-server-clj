(ns elefant-server.api.number)

(defn to-number [potential-number & [parameter-name]]
  (let [exeception-message (str "Number is required for parameter" (if (nil? parameter-name) "" (str " " parameter-name)) " but " (or potential-number "no value") " was supplied.")]
    (if (nil? potential-number)
      (throw (NullPointerException. exeception-message))
      (let [parsed-input (read-string potential-number)]
        (if (number? parsed-input)
          parsed-input
          (throw (IllegalArgumentException. exeception-message))
          )))))
