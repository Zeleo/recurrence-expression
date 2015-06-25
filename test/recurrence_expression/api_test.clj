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
