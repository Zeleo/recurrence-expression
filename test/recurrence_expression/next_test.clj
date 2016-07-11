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

(ns recurrence-expression.next-test
  (:require [clojure.test :refer :all]
            [recurrence-expression.next :refer :all]
            [clj-time.core :as t]))

(deftest test-next-instant
  (let [base-time (t/date-time 2015 3 18 13 27 45)]
    
    (testing "seconds"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (next-instant base-time { :second 45 }))
          "base time same as instant pattern -> next time should equal to base time")
      (is (= (t/date-time 2015 3 18 13 28 0)
             (next-instant base-time { :second 0 }))
          "making sure 0 works")
      (is (= (t/date-time 2015 3 18 13 27 50)
             (next-instant base-time { :second 50 }))
          "base case")
      (is (= (t/date-time 2015 3 18 13 28 44)
             (next-instant base-time { :second 44 }))
          "minute rolling over"))
    
    (testing "minutes"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (next-instant base-time { :minute 27 :second 45 }))
          "base time same as instant pattern -> next time should equal to base time")
      (is (= (t/date-time 2015 3 18 13 27 46)
             (next-instant base-time { :minute 27 :second 46 }))
          "Lower larger than base -> don't roll over")
      (is (= (t/date-time 2015 3 18 14 27 0)
             (next-instant base-time { :minute 27 }))
          "instant pattern implicitly asserts ':second 0'")
      (is (= (t/date-time 2015 3 18 14 0 0)
             (next-instant base-time { :minute 0 }))
          "making sure 0 works")
      (is (= (t/date-time 2015 3 18 13 50 44)
             (next-instant base-time { :minute 50 :second 44 }))
          "base case")
      (is (= (t/date-time 2015 3 18 13 50 46)
             (next-instant base-time { :minute 50 :second 46 }))
          "base case 2")
      (is (= (t/date-time 2015 3 18 14 10 0)
             (next-instant base-time { :minute 10 }))
          "hour rolling over")
      (is (= (t/date-time 2015 3 18 14 10 1)
             (next-instant base-time { :minute 10 :second 1 }))
          "hour rolling over 2")
      (is (= (t/date-time 2015 3 18 14 10 59)
             (next-instant base-time { :minute 10 :second 59 }))
          "hour rolling over 3"))
    
    (testing "hours"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (next-instant base-time { :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> next time should equal to base time")
      (is (= (t/date-time 2015 3 19 13 0 0)
             (next-instant base-time { :hour 13 }))
          "instant pattern implicitly asserts ':minute 0 :second 0'")
      (is (= (t/date-time 2015 3 19 0 0 0)
             (next-instant base-time { :hour 0 }))
          "making sure 0 works")
      (is (= (t/date-time 2015 3 18 14 26 44)
             (next-instant base-time { :hour 14 :minute 26 :second 44 }))
          "base case")
      (is (= (t/date-time 2015 3 18 15 28 46)
             (next-instant base-time { :hour 15 :minute 28 :second 46 }))
          "base case 2")
      (is (= (t/date-time 2015 3 19 10 0 0)
             (next-instant base-time { :hour 10 }))
          "day rolling over")
      (is (= (t/date-time 2015 3 19 12 26 1)
             (next-instant base-time { :hour 12 :minute 26 :second 1 }))
          "day rolling over 2")
      (is (= (t/date-time 2015 3 19 1 28 59)
             (next-instant base-time { :hour 1 :minute 28 :second 59 }))
          "day rolling over 3"))
    
    (testing "days"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (next-instant base-time { :day 18 :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> next time should equal to base time")
      (is (= (t/date-time 2015 4 18 0 0 0)
             (next-instant base-time { :day 18 }))
          "instant pattern implicitly asserts ':hour 0 :minute 0 :second 0'")
      (is (= (t/date-time 2015 4 1 0 0 0)
             (next-instant base-time { :day 1 }))
          "making sure 1 works")
      (is (= (t/date-time 2015 3 20 12 26 44)
             (next-instant base-time { :day 20 :hour 12 :minute 26 :second 44 }))
          "base case")
      (is (= (t/date-time 2015 3 20 14 28 46)
             (next-instant base-time { :day 20 :hour 14 :minute 28 :second 46 }))
          "base case 2")
      (is (= (t/date-time 2015 3 18 14 26 46)
             (next-instant base-time { :day 18 :hour 14 :minute 26 :second 46 }))
          "highest doesn't change but lower change")
      (is (= (t/date-time 2015 4 10 0 0 0)
             (next-instant base-time { :day 10 }))
          "month rolling over")
      (is (= (t/date-time 2015 4 17 12 26 1)
             (next-instant base-time { :day 17 :hour 12 :minute 26 :second 1 }))
          "month rolling over 2")
      (is (= (t/date-time 2015 4 17 1 28 59)
             (next-instant base-time { :day 17 :hour 1 :minute 28 :second 59 }))
          "month rolling over 3"))
    
    (testing "months"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (next-instant base-time { :month 3 :day 18 :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> next time should equal to base time")
      (is (= (t/date-time 2016 3 1 0 0 0)
             (next-instant base-time { :month 3 }))
          "instant pattern implicitly asserts ':day 1 :hour 0 :minute 0 :second 0'")
      (is (= (t/date-time 2016 1 1 0 0 0)
             (next-instant base-time { :month 1 }))
          "making sure 1 works")
      (is (= (t/date-time 2015 4 17 12 26 44)
             (next-instant base-time { :month 4 :day 17 :hour 12 :minute 26 :second 44 }))
          "base case")
      (is (= (t/date-time 2015 4 19 14 28 46)
             (next-instant base-time { :month 4 :day 19 :hour 14 :minute 28 :second 46 }))
          "base case 2")
      (is (= (t/date-time 2016 3 17 14 26 46)
             (next-instant base-time { :month 3 :day 17 :hour 14 :minute 26 :second 46 }))
          "highest doesn't change but lower change")
      (is (= (t/date-time 2016 2 1 0 0 0)
             (next-instant base-time { :month 2 }))
          "month rolling over")
      (is (= (t/date-time 2016 1 17 12 26 1)
             (next-instant base-time { :month 1 :day 17 :hour 12 :minute 26 :second 1 }))
          "month rolling over 2")
      (is (= (t/date-time 2016 1 17 1 28 59)
             (next-instant base-time { :month 1 :day 17 :hour 1 :minute 28 :second 59 }))
          "day rolling over 3"))
    
    (testing "years"
      (is (= (t/date-time 2015 3 18 13 27 45)
             (next-instant base-time { :year 2015 :month 3 :day 18 :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> next time should equal to base time")
      (is (= (t/date-time 2015 1 1 0 0 0)
             (next-instant base-time { :year 2015 }))
          "instant pattern implicitly asserts ':month 1 :day 1 :hour 0 :minute 0 :second 0'")
      (is (= (t/date-time 2020 2 17 12 26 44)
             (next-instant base-time { :year 2020 :month 2 :day 17 :hour 12 :minute 26 :second 44 }))
          "base case")
      (is (= (t/date-time 2020 4 19 14 28 46)
             (next-instant base-time { :year 2020 :month 4 :day 19 :hour 14 :minute 28 :second 46 }))
          "base case 2")
      (is (= (t/date-time 2015 4 17 14 26 46)
             (next-instant base-time { :year 2015 :month 4 :day 17 :hour 14 :minute 26 :second 46 }))
          "highest doesn't change but lower change"))
    ))

(deftest test-next-week
  (let [base-time (t/date-time 2015 3 18 13 27 45)]
    (is (= (t/date-time 2015 3 18 13 27 45)
           (next-instant base-time { :day { :dayOfWeek 3 } :hour 13 :minute 27 :second 45 }))
          "base time same as instant pattern -> previous time should equal to base time")
    (is (= (t/date-time 2015 3 25 0 0 0)
           (next-instant base-time { :day { :dayOfWeek 3 } }))
          "Same day with default hours, minutes, and seconds, this rolls forward to next week")
    (is (= (t/date-time 2015 3 25 0 0 0)
           (next-instant base-time { :day { :dayOfWeek 3 :hour 0 :minute 0 :second 0 } }))
          "Same day with default hours, minutes, and seconds, this rolls forward to next week
default values now explicitly stated")
    (is (= (t/date-time 2015 3 18 13 30 59)
           (next-instant base-time { :day { :dayOfWeek 3 } :hour 13 :minute 30 :second 59 }))
          "Same day but later")
    (is (= (t/date-time 2015 3 25 2 27 46)
           (next-instant base-time { :day { :dayOfWeek 3 } :hour 2 :minute 27 :second 46 }))
          "Same day but earlier time, so we go a week forward")
     (is (= (t/date-time 2015 3 24 0 0 0)
           (next-instant base-time { :day { :dayOfWeek 2 } }))
          "One day day of week earlier (week rolls forward)")
     (is (= (t/date-time 2015 3 19 0 0 0)
           (next-instant base-time { :day { :dayOfWeek 4 } }))
         "One day of week later")

     (let [base-t (t/date-time 2015 4 29 13 27 45)]
       (is (= (t/date-time 2015 5 5 0 0 0)
           (next-instant base-t { :day { :dayOfWeek 2 } }))
         "One day of week earlier, so one week + 1 days later, with month rolling forward"))
     ))

(deftest test-next-nth-week
  (let [base-time (t/date-time 2015 3 18 13 27 45)]
    (is (= (t/date-time 2015 3 18 13 27 45)
           (next-instant base-time
                         { :day { :weekOfMonth 3 :dayOfWeek 3 } :hour 13 :minute 27 :second 45 }))
        "base time same as instant pattern -> previous time should equal to base time")
    
    (is (= (t/date-time 2015 3 19 13 27 45)
           (next-instant base-time
                         { :day { :weekOfMonth 3 :dayOfWeek 4 } :hour 13 :minute 27 :second 45 }))
        "the next day")
    
    (is (= (t/date-time 2015 3 25 13 27 45)
           (next-instant base-time
                         { :day { :weekOfMonth 4 :dayOfWeek 3 } :hour 13 :minute 27 :second 45 }))
        "the next week")
    
    (is (= (t/date-time 2015 4 21 13 27 45)
           (next-instant base-time
                         { :day { :weekOfMonth 3 :dayOfWeek 2 } :hour 13 :minute 27 :second 45 }))
        "the previous day of week, so roll forward month")
    
    (is (= (t/date-time 2015 4 8 13 27 45)
           (next-instant base-time
                         { :day { :weekOfMonth 2 :dayOfWeek 3 } :hour 13 :minute 27 :second 45 }))
        "the previous nth week, so roll forward month")
    
    (is (= (t/date-time 2015 4 21 0 0 0)
           (next-instant base-time
                         { :day { :weekOfMonth 3 :dayOfWeek 2 } }))
        "the previous day of week, so roll forward month")
    
    (is (= (t/date-time 2015 4 8 0 0 0)
           (next-instant base-time
                         { :day { :weekOfMonth 2 :dayOfWeek 3 } }))
        "the previous nth week, so roll forward month")
    ))
