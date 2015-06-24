(ns recurrence-expression.api
  (:require clojure.core)
  (:require [recurrence-expression.core :refer :all])
  (:require [recurrence-expression.data :refer :all])
  (:gen-class :name com.bjondinc.RecurrenceExpression
              :prefix method-
              :main false
              :methods [#^{:static true}
                        [nextTime [org.joda.time.DateTime String]
                         org.joda.time.DateTime]
                        #^{:static true}
                        [nextTime [org.joda.time.DateTime String org.joda.time.DateTime]
                         org.joda.time.DateTime]
                        #^{:static true}
                        [nextTime [org.joda.time.DateTime String org.joda.time.DateTime
                                   org.joda.time.DateTime]
                         org.joda.time.DateTime]
                        #^{:static true}
                        [nextNTimes [org.joda.time.DateTime String int]
                         java.util.List]
                        #^{:static true}
                        [nextNTimes [org.joda.time.DateTime String int org.joda.time.DateTime]
                         java.util.List]
                        #^{:static true}
                        [nextNTimes [org.joda.time.DateTime String int org.joda.time.DateTime
                                     org.joda.time.DateTime]
                         java.util.List]
                        ]))

(defn method-nextTime
  ([current-time pattern-string]
     (let [pattern (from-json pattern-string)]
       (next-time current-time pattern)))
  ([current-time pattern-string start-time]
     (let [pattern (from-json pattern-string)]
       (next-time current-time pattern start-time)))
  ([current-time pattern-string start-time end-time]
     (let [pattern (from-json pattern-string)]
       (next-time current-time pattern start-time end-time))))

(defn method-nextNTimes
  ([current-time pattern-string num-times]
     (let [pattern (from-json pattern-string)]
       (next-n-times current-time pattern num-times)))
  ([current-time pattern-string num-times start-time]
     (let [pattern (from-json pattern-string)]
       (next-n-times current-time pattern num-times start-time)))
  ([current-time pattern-string num-times start-time end-time]
     (let [pattern (from-json pattern-string)]
       (next-n-times current-time pattern num-times start-time end-time))))
