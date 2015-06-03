(ns recurrence-expression.data
  (:require clojure.core clojure.pprint)
  (:require [schema.core :as s])
  (:require [clojure.data.json :as j])
  )

(def Instant
  {
   (s/optional-key :year) s/Int
   (s/optional-key :month) s/Int
   (s/optional-key :day) s/Int
   (s/optional-key :hour) s/Int
   (s/optional-key :minute) s/Int
   (s/optional-key :second) s/Int
   })

(def Boundary
  {
   :from Instant
   :to Instant
   })

(def Interval (assoc Instant (s/optional-key :week) s/Int))

(def Recurrence
  {
   (s/optional-key :year) (s/either s/Int [s/Int])
   (s/optional-key :month) (s/either s/Int [s/Int])
   (s/optional-key :day) (s/either s/Int [s/Int])
   (s/optional-key :hour) (s/either s/Int [s/Int])
   (s/optional-key :minute) (s/either s/Int [s/Int])
   (s/optional-key :second) (s/either s/Int [s/Int])
   })

(def Schedule
  (s/either { (s/optional-key :every) Interval
              (s/optional-key :repeat) (s/either Recurrence [Recurrence]) }
            
            { (s/optional-key :between) (s/either Boundary [Boundary])
              (s/optional-key :repeat) (s/either Recurrence [Recurrence]) }))

(defn from-json [json]
  (j/read-str json :key-fn keyword))

(defn to-json [schedule]
  (j/write-str schedule))
