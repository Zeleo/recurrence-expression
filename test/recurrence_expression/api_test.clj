(ns recurrence-expression.api-test
  (:require [clojure.test :refer :all]
            [recurrence-expression.core :refer :all]
            [recurrence-expression.data :refer :all]
            [clj-time.core :as t])
  (:import (clojure.lang ExceptionInfo))
  (:import (com.bjondinc RecurrenceExpression)))

(deftest test-java-api-basic
  (let [schedule
        "{ \"every\": { \"month\": 13 },
           \"at\": { \"day\": { \"weekOfMonth\": 3, \"dayOfWeek\": 5 } } }"
        start-time (t/date-time 2015 4 14 10 35 39)
        current-time (t/plus start-time (t/seconds 2))
        expected-time (t/date-time 2015 4 17 0 0 0)]
    (is (= expected-time
           (RecurrenceExpression/nextTime current-time
                           schedule
                           start-time
                           max-date-time)))))
