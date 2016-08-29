;; Copyright (c) 2015 Bjönd, Inc.
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
(comment

  ;; Circa mid-August 2016, I disabled AOT compilation.
  ;; So the RecurrenceExpression java class isn't
  ;; available for unit testing.
  ;; As we don't currently use that class in production,
  ;; I'm just going to comment out the test.

  (ns recurrence-expression.api-test
    (:require [clojure.test :refer :all]
              [recurrence-expression.core :refer :all]
              [recurrence-expression.data :refer :all]
              [clj-time.core :as t])
    (:import (clojure.lang ExceptionInfo))
    (:import (com.bjondinc RecurrenceExpression)))

;;; Note, I'm simply testing here if the calls to java wrapper
;;; succeeds or not.
  
  (deftest test-java-api-basic
    (let [schedule
          "{ \"at\": { \"day\": 5 } }"
          start-time (t/date-time 2015 4)
          end-time (t/date-time 2015 7)
          current-time (t/plus start-time (t/seconds 1))]
      
      (let [expected-time (t/date-time 2015 4 5)]
        (let [result (RecurrenceExpression/nextTime current-time
                                                    schedule)]
          (is (= expected-time
                 result)))
        
        (let [result (RecurrenceExpression/nextTime current-time
                                                    schedule
                                                    start-time)]
          (is (= expected-time
                 result)))
        
        (let [result (RecurrenceExpression/nextTime current-time
                                                    schedule
                                                    start-time
                                                    end-time)]
          (is (= expected-time
                 result)))
        )

      (let [expected-times [(t/date-time 2015 4 5)
                            (t/date-time 2015 5 5)]]
        (let [result (RecurrenceExpression/nextNTimes current-time
                                                      schedule
                                                      2)]
          (is (= expected-times
                 result)))
        
        (let [result (RecurrenceExpression/nextNTimes current-time
                                                      schedule
                                                      2
                                                      start-time)]
          (is (= expected-times
                 result)))
        
        (let [result (RecurrenceExpression/nextNTimes current-time
                                                      schedule
                                                      2
                                                      start-time
                                                      end-time)]
          (is (= expected-times
                 result)))
        )
      ))
  )
