;; Copyright (c) 2015--2016 Bjönd, Inc.
;;
;; This file is part of Recurrence Expression.
;;
;; Recurrence Expression is free software: you can redistribute it
;; and/or modify it under the terms of the GNU Lesser General Public
;; License as published by the Free Software Foundation, either
;; version 3 of the License, or (at your option) any later version.
;;
;; Recurrence Expression is distributed in the hope that it will be
;; useful, but WITHOUT ANY WARRANTY; without even the implied warranty
;; of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with Recurrence Expression.  If not, see
;; <http://www.gnu.org/licenses/>.

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
                        [nextTimeA [org.joda.time.DateTime Object]
                         org.joda.time.DateTime]
                        #^{:static true}
                        [nextTimeA [org.joda.time.DateTime Object org.joda.time.DateTime]
                         org.joda.time.DateTime]
                        #^{:static true}
                        [nextTimeA [org.joda.time.DateTime Object org.joda.time.DateTime
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
                        
                        #^{:static true}
                        [nextNTimesA [org.joda.time.DateTime Object int]
                         java.util.List]
                        #^{:static true}
                        [nextNTimesA [org.joda.time.DateTime Object int org.joda.time.DateTime]
                         java.util.List]
                        #^{:static true}
                        [nextNTimesA [org.joda.time.DateTime Object int org.joda.time.DateTime
                                     org.joda.time.DateTime]
                         java.util.List]
                        
                        #^{:static true}
                        [fromJSON [String] Object]
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

(defn method-nextTimeA
  ([current-time pattern]
     (next-time current-time pattern))
  ([current-time pattern start-time]
     (next-time current-time pattern start-time))
  ([current-time pattern start-time end-time]
     (next-time current-time pattern start-time end-time)))

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

(defn method-nextNTimesA
  ([current-time pattern num-times]
     (next-n-times current-time pattern num-times))
  ([current-time pattern num-times start-time]
     (next-n-times current-time pattern num-times start-time))
  ([current-time pattern num-times start-time end-time]
     (next-n-times current-time pattern num-times start-time end-time)))

(defn method-fromJSON
  [json]
  (from-json json))
