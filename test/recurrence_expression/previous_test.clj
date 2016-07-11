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

(ns recurrence-expression.previous-test
  (:require [clojure.test :refer :all]
            [recurrence-expression.previous :refer :all]
            [clj-time.core :as t]))

(deftest test-previous
  (let [base-time (t/date-time 2015 3 18 13 27 45)]
    
    (testing "seconds"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (previous base-time { :second 45 }))
          "base time same as instant pattern -> previous time should equal to base time")
      (is (= (t/date-time 2015 3 18 13 27 0)
             (previous base-time { :second 0 }))
          "making sure 0 works")
      (is (= (t/date-time 2015 3 18 13 27 10)
             (previous base-time { :second 10 }))
          "base case")
      (is (= (t/date-time 2015 3 18 13 26 46)
             (previous base-time { :second 46 }))
          "minute rolling back")
      (is (= (t/date-time 2015 3 18 13 26 59)
             (previous base-time { :second 59 }))
          "minute rolling back"))
    
    (testing "minutes"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (previous base-time { :minute 27 :second 45 }))
          "base time same as instant pattern -> previous time should equal to base time")
      (is (= (t/date-time 2015 3 18 13 27 0)
             (previous base-time { :minute 27 }))
          "instant pattern implicitly asserts ':second 0'")
      (is (= (t/date-time 2015 3 18 13 0 0)
             (previous base-time { :minute 0 }))
          "making sure 0 works")
      (is (= (t/date-time 2015 3 18 13 15 0)
             (previous base-time { :minute 15 }))
          "base case")
      (is (= (t/date-time 2015 3 18 12 28 0)
             (previous base-time { :minute 28 }))
          "hour rolling back")
      (is (= (t/date-time 2015 3 18 12 59 0)
             (previous base-time { :minute 59 }))
          "hour rolling back"))
    
    (testing "hours"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (previous base-time { :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> previous time should equal to base time")
      (is (= (t/date-time 2015 3 18 13 0 0)
             (previous base-time { :hour 13 }))
          "instant pattern implicitly asserts ':minute 0 :second 0'")
      (is (= (t/date-time 2015 3 18 0 0 0)
             (previous base-time { :hour 0 }))
          "making sure 0 works")
      (is (= (t/date-time 2015 3 18 7 0 0)
             (previous base-time { :hour 7 }))
          "base case")
      (is (= (t/date-time 2015 3 17 14 0 0)
             (previous base-time { :hour 14 }))
          "day rolling back")
      (is (= (t/date-time 2015 3 17 23 0 0)
             (previous base-time { :hour 23 }))
          "day rolling back"))
    
    (testing "days"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (previous base-time { :day 18 :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> previous time should equal to base time")
      (is (= (t/date-time 2015 3 18 0 0 0)
             (previous base-time { :day 18 }))
          "instant pattern implicitly asserts ':hour 0 :minute 0 :second 0'")
      (is (= (t/date-time 2015 3 1 0 0 0)
             (previous base-time { :day 1 }))
          "making sure 1 works")
      (is (= (t/date-time 2015 3 7 0 0 0)
             (previous base-time { :day 7 }))
          "base case")
      (is (= (t/date-time 2015 2 19 0 0 0)
             (previous base-time { :day 19  }))
          "month rolling back"))
    
    (testing "months"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (previous base-time { :month 3 :day 18 :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> previous time should equal to base time")
      (is (= (t/date-time 2015 3 1 0 0 0)
             (previous base-time { :month 3 }))
          "instant pattern implicitly asserts ':day 1 :hour 0 :minute 0 :second 0'")
      (is (= (t/date-time 2015 1 1 0 0 0)
             (previous base-time { :month 1 }))
          "making sure 1 works")
      (is (= (t/date-time 2015 2 1 0 0 0)
             (previous base-time { :month 2 }))
          "base case")
      (is (= (t/date-time 2014 7 1 0 0 0)
             (previous base-time { :month 7  }))
          "month rolling back")
      (is (= (t/date-time 2014 12 1 0 0 0)
             (previous base-time { :month 12  }))
          "month rolling back"))
    
    (testing "years"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (previous base-time { :year 2015 :month 3 :day 18 :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> previous time should equal to base time")
      (is (= (t/date-time 2015 1 1 0 0 0)
             (previous base-time { :year 2015 }))
          "instant pattern implicitly asserts ':month 1 :day 1 :hour 0 :minute 0 :second 0'")
      (is (= (t/date-time 2022 1 1 0 0 0)
             (previous (t/date-time 2030) { :year 2022 }))
          "base case"))
    ))

(deftest test-previous-end-of-month-days
  (let [base-time (t/date-time 2015 3 28 13 27 45)]
      (is (= (t/date-time 2015 1 29 0 0 0)
             (previous base-time { :day 29  }))
          "month rolling back twice because 2/29/2015 does not exist")))

(deftest test-previous-week
  (let [base-time (t/date-time 2015 3 18 13 27 45)]
    (is (= (t/date-time 2015 3 18 13 27 45)
           (previous base-time { :day { :dayOfWeek 3 } :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> previous time should equal to base time")
    (is (= (t/date-time 2015 3 18 0 0 0)
           (previous base-time { :day { :dayOfWeek 3 } }))
          "Same day with default hours, minutes, and seconds")
    (is (= (t/date-time 2015 3 18 2 30 59)
           (previous base-time { :day { :dayOfWeek 3 } :hour 2 :minute 30 :second 59 }))
          "Same day but earlier")
    (is (= (t/date-time 2015 3 11 13 27 46)
           (previous base-time { :day { :dayOfWeek 3 } :hour 13 :minute 27 :second 46 }))
          "Same day but later time, so we go a week back")
     (is (= (t/date-time 2015 3 17 0 0 0)
           (previous base-time { :day { :dayOfWeek 2 } }))
          "One day earlier")
     (is (= (t/date-time 2015 3 12 0 0 0)
           (previous base-time { :day { :dayOfWeek 4 } }))
         "One day of week later, so one week - 1 days earlier")

     (let [base-t (t/date-time 2015 3 4 13 27 45)]
       (is (= (t/date-time 2015 2 26 0 0 0)
           (previous base-t { :day { :dayOfWeek 4 } }))
         "One day of week later, so one week - 1 days earlier, with month rolling back"))
    ))

(deftest test-previous-nth-week
  (let [base-time (t/date-time 2015 3 18 13 27 45)]
    (is (= (t/date-time 2015 3 18 13 27 45)
           (previous base-time
                         { :day { :weekOfMonth 3 :dayOfWeek 3 } :hour 13 :minute 27 :second 45 }))
        "base time same as instant pattern -> previous time should equal to base time")
    
    (is (= (t/date-time 2015 3 17 13 27 45)
           (previous base-time
                         { :day { :weekOfMonth 3 :dayOfWeek 2 } :hour 13 :minute 27 :second 45 }))
        "the previous day")
    
    (is (= (t/date-time 2015 3 11 13 27 45)
           (previous base-time
                         { :day { :weekOfMonth 2 :dayOfWeek 3 } :hour 13 :minute 27 :second 45 }))
        "the previous week")
    
    (is (= (t/date-time 2015 2 19 13 27 45)
           (previous base-time
                         { :day { :weekOfMonth 3 :dayOfWeek 4 } :hour 13 :minute 27 :second 45 }))
        "the next day of week, so roll back month")
    
    (is (= (t/date-time 2015 2 25 13 27 45)
           (previous base-time
                         { :day { :weekOfMonth 4 :dayOfWeek 3 } :hour 13 :minute 27 :second 45 }))
        "the next nth week, so roll back month")
    
    (is (= (t/date-time 2015 2 19 0 0 0)
           (previous base-time
                         { :day { :weekOfMonth 3 :dayOfWeek 4 } }))
        "the previous day of week, so roll forward month")
    
    (is (= (t/date-time 2015 2 25 0 0 0)
           (previous base-time
                         { :day { :weekOfMonth 4 :dayOfWeek 3 } }))
        "the previous nth week, so roll forward month")
    ))
