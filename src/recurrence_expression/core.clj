(ns recurrence-expression.core
  (:require clojure.core)
  (:require [clojure.pprint :as pp])
  (:require [clojure.math.numeric-tower :as nt])
  (:require [clj-time.core :as t])
  (:require [clj-time.format :as tf])
  (:require [clj-time.coerce :as tc])
  (:require [clj-time.periodic :as tp])
  (:require [schema.core :as s])
  (:require [recurrence-expression.data :refer :all])
  (:import (org.joda.time DateTime DateTimeZone))
  (:import (java.util Calendar)))

;; {
;;  :every { :week 13 } 
;;  }

;; 3rd Monday of the month.
;; {
;;  :day { :dayOfWeek 1 :weekOfMonth 3 }
;;  }
;; Wendesday and Friday of 2nd week
;;  { :dayOfWeek [ 3, 5 ] :weekOfMonth 2 }
;;

;; order matters in this list.
(def instant-property-list
  [
   :second
   :minute
   :hour
   :day
   :month
   :year
   ])

(def instant-property-index-map
  (reduce
   #(assoc %1 %2 (.indexOf instant-property-list %2))
   {}
   instant-property-list))

(def instant-property-ranges
  {
   :second { :min 0 :max 59 }
   :minute { :min 0 :max 59 }
   :hour { :min 0 :max 23 }
   :day { :min 1 :max 31 }
   :month { :min 1 :max 12 }
   :year { :min 2015 :max (+ 100 (t/year (t/now))) }
   })

(def max-date-time (t/minus
                    (t/date-time (+ 1 (:max (:year instant-property-ranges))))
                    (t/seconds 1)))

(def min-date-time (t/date-time 2015 1 1))

(defn highest-order-property-defined [instant-pattern]
  (let [property-keys (keys instant-pattern)]
    (cond
     (empty? property-keys) nil
     (= 1 count property-keys) (first property-keys)
     :else (reduce #(let [index1 (get instant-property-index-map %1)
                     index2 (get instant-property-index-map %2)]
                 (if (> index1 index2)
                   %1
                   %2))
                      property-keys))))

(defn value-or-default [value default]
  (if (not (nil? value))
    value
    default))

(defn zero-out-millis [dt]
  (let [mil (t/milli dt)]
    (t/minus dt (t/millis mil))))

(defn nth-week [year month n dayOfWeek]
  ;; 1 <= n <= 5
  ;; may return nil if no such day exists in a month
  (let [first-day (t/first-day-of-the-month year month)
        first-day-of-week (t/day-of-week first-day)
        diff (if (<= first-day-of-week dayOfWeek)
               (- dayOfWeek first-day-of-week)
               (+ (- 7 first-day-of-week) dayOfWeek))
        first-dayOfWeek (t/plus first-day (t/days diff))
        nth-dayOfWeek (t/plus first-dayOfWeek (t/weeks (dec n)))]
    (if (= month (t/month nth-dayOfWeek))
      nth-dayOfWeek
      nil)))

(defn previous-nth-week [base-time n day-of-week]
  (if (> n 5)
    (throw (IllegalArgumentException.
            (str "Invalid day-of-week: " n))))
  (loop [nth nil
         base-t base-time]
    (if nth
      nth
      (let [year (t/year base-t)
            month (t/month base-t)
            d (nth-week year month n day-of-week)
            day (t/date-time (t/year d)
                        (t/month d)
                        (t/day d)
                        (t/hour base-t)
                        (t/minute base-t)
                        (t/second base-t))]
        (recur
         (if (and day (or (= day base-time) (t/before? day base-time)))
           day
           nil)
         (t/minus base-t (t/months 1)))))))

(defn next-nth-week [base-time n day-of-week]
  (if (> n 5)
    (throw (IllegalArgumentException.
            (str "Invalid day-of-week: " n))))
  (loop [nth nil
         base-t base-time]
    (if nth
      nth
      (let [year (t/year base-t)
            month (t/month base-t)
            d (nth-week year month n day-of-week)
            day (t/date-time (t/year d)
                        (t/month d)
                        (t/day d)
                        (t/hour base-t)
                        (t/minute base-t)
                        (t/second base-t))]
        (recur
         (if (and day (or (= day base-time) (t/after? day base-time)))
           day
           nil)
         (t/plus base-t (t/months 1)))))))

(defn previous-day-of-week [base-time day-of-week]
  (let [base-day-of-week (t/day-of-week base-time)
        this-many (if (< base-day-of-week day-of-week)
                    (+ (- 7 day-of-week) base-day-of-week)
                    (- base-day-of-week day-of-week))]
    (t/minus base-time (t/days this-many))))

(defn next-day-of-week [base-time day-of-week]
  (let [base-day-of-week (t/day-of-week base-time)]
    (if (= base-day-of-week day-of-week)
      base-time
      (let [this-many (if (< base-day-of-week day-of-week)
                        (- base-day-of-week day-of-week)
                        (+ (- 7 base-day-of-week) day-of-week))]
        (t/plus base-time (t/days this-many))))))

;; Tuesdays
;; { :dayOfWeek 3 }
;;
;; Sunday of the 3rd week of the month
;; { :weekOfMonth 3 }
;;
;; 3rd Monday of the month.
;; { :dayOfWeek 2 :weekOfMonth 3 }
;;
(defn previous-x-of-week [base-time week-pattern]
  (let [day-of-week (value-or-default (get week-pattern :dayOfWeek) 1)
        week-of-month (value-or-default (get week-pattern :weekOfMonth) nil)]
    (if week-of-month
      (previous-nth-week base-time week-of-month day-of-week)
      (previous-day-of-week base-time day-of-week))))

(defn next-x-of-week [base-time week-pattern]
  (let [day-of-week (value-or-default (get week-pattern :dayOfWeek) 1)
        week-of-month (value-or-default (get week-pattern :weekOfMonth) nil)]
    (if week-of-month
      (next-nth-week base-time week-of-month day-of-week)
      (next-day-of-week base-time day-of-week))))

(defn safe-create-date [year month day hour minute second]
  (let [num-days (t/number-of-days-in-the-month year month)]
    (if (> day num-days)
      nil
      (t/date-time year month day hour minute second))))

(defn previous-day-of-month [base-time day-of-month]
  (if (> day-of-month 31)
    (throw (IllegalArgumentException.
            (str "Invalid day-of-month: " day-of-month))))
  (loop [the-day nil
         base-t base-time]
    (if the-day
      the-day
      (let [base-day (t/day base-t)
            month-prior (t/minus base-t (t/months 1))]
        (recur (if (>= base-day day-of-month)
                 (let [diff (- base-day day-of-month)]
                   (t/minus base-t (t/days diff)))
                 (safe-create-date (t/year month-prior)
                                   (t/month month-prior)
                                   day-of-month
                                   (t/hour month-prior)
                                   (t/minute month-prior)
                                   (t/second month-prior)))
               month-prior)))))

(defn next-day-of-month [base-time day-of-month]
  (if (> day-of-month 31)
    (throw (IllegalArgumentException.
            (str "Invalid day-of-month: " day-of-month))))
  (loop [the-day nil
         base-t base-time]
    (if the-day
      the-day
      (let [base-day (t/day base-t)
            next-month (t/plus base-t (t/months 1))]
        (recur (if (<= base-day day-of-month)
                 (let [diff (- day-of-month base-day)]
                   (safe-create-date (t/year base-t)
                                     (t/month base-t)
                                     (+ base-day diff)
                                     (t/hour base-t)
                                     (t/minute base-t)
                                     (t/second base-t)))
                 (safe-create-date (t/year next-month)
                                   (t/month next-month)
                                   day-of-month
                                   (t/hour next-month)
                                   (t/minute next-month)
                                   (t/second next-month)))
               next-month)))))

;; Tuesdays
;; {
;;  :day { :dayOfWeek 3 }
;;  }
;; Sunday of the 3rd week of the month
;; {
;;  :day { :weekOfMonth 3 }
;;  }
;; 3rd Monday of the month.
;; {
;;  :day { :dayOfWeek 2 :weekOfMonth 3 }
;;  }
;;
;; Wendesday and Friday of 2nd week
;; {
;;  :day { :dayOfWeek [ 4, 6 ] :weekOfMonth 2 }
;;  }
(defn previous-day [base-time day-pattern]
  (cond
   (number? day-pattern) (previous-day-of-month base-time day-pattern)
   (map? day-pattern) (previous-x-of-week base-time day-pattern)
   :else (throw (IllegalArgumentException.
                    (str "Invalid day-pattern: " day-pattern)))))

(defn next-day-instant [base-time day-pattern]
  (cond
   (number? day-pattern) (next-day-of-month base-time day-pattern)
   (map? day-pattern) (next-x-of-week base-time day-pattern)
   :else (throw (IllegalArgumentException.
                 (str "Invalid day-pattern: " day-pattern)))))

(defmulti next-day-occurrence
  (fn [base-time day-pattern]
    (cond
     (number? day-pattern) :single
     (map? day-pattern) :single
     (sequential? day-pattern) :multiple
     :else (throw (IllegalArgumentException.
                   (str "Invalid day-pattern: " day-pattern))))))

(defmethod next-day-occurrence :single [base-time day-pattern]
  (let [next (next-day-instant base-time day-pattern)]
    (if (and (= (t/year base-time) (t/year next))
             (= (t/month base-time) (t/month next)))
      [next false] ;; didn't roll-over
      [(t/date-time (t/year next)
                    (t/month next)
                    1
                    0
                    0
                    0)
       true])))

(defmethod next-day-occurrence :multiple [base-time day-pattern]
  (let [next-time-results (map #(next-day-occurrence base-time %) day-pattern)]
    (reduce #(if (t/before? (first %1) (first %2)) %1 %2) next-time-results)))

(defn compare-days [date-time instant-pattern]
  (let [dt-day (t/day date-time)
        ip-day (get instant-pattern :day)]
    (compare dt-day ip-day)))

(defmulti previous-unit-value (fn [time-unit-key base-time instant-pattern]
                                time-unit-key))

(defmethod previous-unit-value :year [time-unit-key base-time instant-pattern]
  (t/date-time (if (contains? instant-pattern time-unit-key)
                 (get instant-pattern :year)
                 (t/year base-time))
               (t/month base-time)
               (t/day base-time)
               (t/hour base-time)
               (t/minute base-time)
               (t/second base-time)))

(defmethod previous-unit-value :month [time-unit-key base-time instant-pattern]
  (if (contains? instant-pattern time-unit-key)
    (let [rollback (< (t/month base-time) (get instant-pattern :month))]
      (t/date-time (if rollback
                     (t/year (t/minus base-time (t/years 1)))
                     (t/year base-time))
                   (get instant-pattern :month)
                   (t/day base-time)
                   (t/hour base-time)
                   (t/minute base-time)
                   (t/second base-time)))
    (t/date-time (t/year base-time)
                 1
                 (t/day base-time)
                 (t/hour base-time)
                 (t/minute base-time)
                 (t/second base-time))))

(defmethod previous-unit-value :day [time-unit-key base-time instant-pattern]
  (if (contains? instant-pattern time-unit-key)
    (previous-day base-time (get instant-pattern time-unit-key))
    (t/date-time (t/year base-time)
                 (t/month base-time)
                 1
                 (t/hour base-time)
                 (t/minute base-time)
                 (t/second base-time))))

(defmethod previous-unit-value :hour [time-unit-key base-time instant-pattern]
  (if (contains? instant-pattern time-unit-key)
    (let [rollback (< (t/hour base-time) (get instant-pattern :hour))
          t (if rollback
              (t/minus base-time (t/days 1))
              base-time)]
      (t/date-time (t/year t)
                   (t/month t)
                   (t/day t)
                   (get instant-pattern :hour)
                   (t/minute t)
                   (t/second t)))
    (t/date-time (t/year base-time)
                 (t/month base-time)
                 (t/day base-time)
                 0
                 (t/minute base-time)
                 (t/second base-time))))

(defmethod previous-unit-value :minute [time-unit-key base-time instant-pattern]
  (if (contains? instant-pattern time-unit-key)
    (let [rollback (< (t/minute base-time) (get instant-pattern :minute))
          t (if rollback
              (t/minus base-time (t/hours 1))
              base-time)]
      (t/date-time (t/year t)
                   (t/month t)
                   (t/day t)
                   (t/hour t)
                   (get instant-pattern :minute)
                   (t/second t)))
    (t/date-time (t/year base-time)
                 (t/month base-time)
                 (t/day base-time)
                 (t/hour base-time)
                 0
                 (t/second base-time))))

(defmethod previous-unit-value :second [time-unit-key base-time instant-pattern]
  (if (contains? instant-pattern time-unit-key)
    (let [rollback (< (t/second base-time) (get instant-pattern :second))
          t (if rollback
              (t/minus base-time (t/minutes 1))
              base-time)]
      (t/date-time (t/year t)
                   (t/month t)
                   (t/day t)
                   (t/hour t)
                   (t/minute t)
                   (get instant-pattern :second)))
    (t/date-time (t/year base-time)
                 (t/month base-time)
                 (t/day base-time)
                 (t/hour base-time)
                 (t/minute base-time)
                 0)))

(defmethod previous-unit-value :default [time-unit-key base-time instant-pattern]
  (throw (IllegalArgumentException. (str "Invalid time unit: " time-unit-key))))

(defn previous [base-time instant-pattern]
  (let [highest-order-property (highest-order-property-defined instant-pattern)
        highest-order-property-index (get instant-property-index-map highest-order-property)]
    (loop [time base-time
           properties instant-property-list]
      (if (or (empty? properties) (< highest-order-property-index
                                     (get instant-property-index-map (first properties))))
        time
        (let [property (first properties)
              prev-time (previous-unit-value property time instant-pattern)]
          (recur prev-time
                 (rest properties)))))))

(defmulti next-unit-value (fn [time-unit-key base-time instant-pattern]
                         time-unit-key))

(defmethod next-unit-value :year [time-unit-key base-time instant-pattern]
  (t/date-time (value-or-default (get instant-pattern time-unit-key) (t/year base-time))
               (t/month base-time)
               (t/day base-time)
               (t/hour base-time)
               (t/minute base-time)
               (t/second base-time)))

(defmethod next-unit-value :month [time-unit-key base-time instant-pattern]
  (let [unit-value (value-or-default (get instant-pattern time-unit-key) 1)
        rollover (> (t/month base-time) unit-value)
        t (if rollover
            (t/plus base-time (t/years 1))
            base-time)]
    (t/date-time (t/year t)
                 unit-value
                 (t/day t)
                 (t/hour t)
                 (t/minute t)
                 (t/second t))))

(defmethod next-unit-value :day [time-unit-key base-time instant-pattern]
  (if (contains? instant-pattern time-unit-key)
    (next-day-instant base-time (get instant-pattern time-unit-key))
    (let [current-day (t/day base-time)
          roll-forward (> current-day 1)]
      (if roll-forward (let [t (t/plus base-time (t/months 1))]
                        (t/date-time (t/year t)
                                     (t/month t)
                                     1
                                     (t/hour t)
                                     (t/minute t)
                                     (t/second t)))
          base-time))))

(defmethod next-unit-value :hour [time-unit-key base-time instant-pattern]
  (let [unit-value (value-or-default (get instant-pattern time-unit-key) 0)
        rollover (> (t/hour base-time) unit-value)
        t (if rollover
            (t/plus base-time (t/days 1))
            base-time)]
    (t/date-time (t/year t)
                 (t/month t)
                 (t/day t)
                 unit-value
                 (t/minute t)
                 (t/second t))))

(defmethod next-unit-value :minute [time-unit-key base-time instant-pattern]
  (let [unit-value (value-or-default (get instant-pattern time-unit-key) 0)
        rollover (> (t/minute base-time) unit-value)
        t (if rollover
            (t/plus base-time (t/hours 1))
            base-time)]
    (t/date-time (t/year t)
                 (t/month t)
                 (t/day t)
                 (t/hour t)
                 unit-value
                 (t/second t))))

(defmethod next-unit-value :second [time-unit-key base-time instant-pattern]
  (let [unit-value (value-or-default (get instant-pattern time-unit-key) 0)
        rollover (> (t/second base-time) unit-value)
        t (if rollover (t/plus base-time (t/minutes 1))
              base-time)]
    (t/date-time (t/year t)
                 (t/month t)
                 (t/day t)
                 (t/hour t)
                 (t/minute t)
                 unit-value)))
  
(defmethod next-unit-value :default [time-unit-key base-time instant-pattern]
  (throw (IllegalArgumentException. (str "Invalid time unit: " time-unit-key))))

(defn next-instant [base-time instant-pattern]
  (let [highest-order-property (highest-order-property-defined instant-pattern)
        highest-order-property-index (get instant-property-index-map highest-order-property)]
    (loop [time base-time
           properties instant-property-list]
      (if (or (empty? properties) (< highest-order-property-index
                                     (get instant-property-index-map (first properties))))
        time
        (let [property (first properties)
              next-time (next-unit-value property time instant-pattern)]
          (recur next-time
                 (rest properties)))))))

(defmulti included? (fn [time interval-pattern]
                      (cond (sequential? interval-pattern) :multiple
                            (map? interval-pattern) :single
                            (or (nil? interval-pattern) (empty? interval-pattern)) :empty
                            :else (throw (IllegalArgumentException.
                                             (str "Invalid argument: " interval-pattern))))))

(defmethod included? :multiple [time interval-pattern]
  (some #(included? time %) interval-pattern))
                      
(defmethod included? :single [time interval-pattern]
  "True if time sits inside interval described by interval pattern"
  (let [lower (previous time (get interval-pattern :from))
        upper (next-instant lower (get interval-pattern :to))
        included (or
                  (and (t/before? lower time)
                       (t/before? time upper))
                  (= lower time)
                  (= upper time))
        nested-interval (get interval-pattern :between)]
    (if included
      (if (nil? nested-interval)
        true
        (included? time nested-interval))
      false)))

(defmethod included? :empty [time interval-pattern]
  true)

(defmulti next-included-time (fn [time interval-pattern]
                      (cond (sequential? interval-pattern) :multiple
                            (map? interval-pattern) :single
                            (or (nil? interval-pattern) (empty? interval-pattern)) :empty
                            :else (throw (IllegalArgumentException.
                                             (str "Invalid argument: " interval-pattern))))))

(defmethod next-included-time :multiple [time interval-pattern]
  (let [times (map #(next-included-time time %) interval-pattern)
        sorted (sort times)]
    (if (empty? sorted)
      (throw (Exception. "Maximum time exceeded while computing next-included-time")))
    (first sorted)))

(defmethod next-included-time :single [time interval-pattern]
  "It's assumed here that time is outside interval-pattern"
  (next-instant time (get interval-pattern :from)))

(defmethod next-included-time :empty [time interval-pattern]
  time)
  
(defn compile-unit-pattern [unit-pattern prop all]
  (cond
   (nil? unit-pattern) (into (sorted-set)
                             (if all
                               (range (:min (prop instant-property-ranges))
                                      (inc (:max (prop instant-property-ranges))))
                               [(:min (prop instant-property-ranges))]))
   (number? unit-pattern) (sorted-set unit-pattern)
   (vector? unit-pattern) (into (sorted-set) unit-pattern)
   ;; TODO: implement more unit-patterns
   :else (throw (IllegalArgumentException.
                    (str "Invalid unit-pattern: " unit-pattern)))))

(defn compile-recurrence-pattern [recurrence-pattern]
  (let [highest-order-property (highest-order-property-defined recurrence-pattern)
        highest-order-property-index (get instant-property-index-map highest-order-property)]
    (loop [props instant-property-list
           compiled-patterns {}]
      (if (empty? props)
        compiled-patterns
        (let [prop (first props)
              prop-index (get instant-property-index-map prop)
              pattern (get recurrence-pattern prop)
              all (>= prop-index highest-order-property-index)]
          (recur
           (rest props)
           (assoc
               compiled-patterns
             prop (if (= prop :day)
                    pattern ;; We don't compile :day field.
                    (compile-unit-pattern pattern prop all)))))))))

(defn next-value [current-value compiled-pattern]
  (let [v (subseq compiled-pattern >= current-value)]
    (if (empty? v)
      :roll-over
      (first v))))

(defn next-second [current-time compiled-recurrence-pattern recurrence-pattern]
  (let [pattern (get compiled-recurrence-pattern :second)
        current (t/second current-time)
        next-value (next-value current pattern)]
    (if (= next-value :roll-over)
      [(t/plus (t/date-time (t/year current-time)
                            (t/month current-time)
                            (t/day current-time)
                            (t/hour current-time)
                            (t/minute current-time)
                            0)
               (t/minutes 1))
       true]
      [(let [increment (- next-value current)]
         (t/plus current-time (t/seconds increment)))
       false])))

(defn next-minute [current-time compiled-recurrence-pattern recurrence-pattern]
  (let [pattern (get compiled-recurrence-pattern :minute)
        current (t/minute current-time)
        next-value (next-value current pattern)]
    (if (= next-value :roll-over)
      [(t/plus (t/date-time (t/year current-time)
                            (t/month current-time)
                            (t/day current-time)
                            (t/hour current-time)
                            0
                            0)
               (t/hours 1))
       true]
      [(t/date-time (t/year current-time)
                    (t/month current-time)
                    (t/day current-time)
                    (t/hour current-time)
                    next-value
                    (t/second current-time))
       false])))

(defn next-hour [current-time compiled-recurrence-pattern recurrence-pattern]
  (let [pattern (get compiled-recurrence-pattern :hour)
        current (t/hour current-time)
        next-value (next-value current pattern)]
    (if (= next-value :roll-over)
      [(t/plus (t/date-time (t/year current-time)
                            (t/month current-time)
                            (t/day current-time)
                            0
                            0
                            0)
               (t/days 1))
       true]
      [(t/date-time (t/year current-time)
                    (t/month current-time)
                    (t/day current-time)
                    next-value
                    (t/minute current-time)
                    (t/second current-time))
       false])))

(defn next-day [current-time compiled-recurrence-pattern recurrence-pattern]
  (let [day-pattern (get recurrence-pattern :day)]
    (if day-pattern
      (next-day-occurrence current-time day-pattern)
      (let [day-index (get instant-property-index-map :day)
            highest-order-property (highest-order-property-defined recurrence-pattern)
            highest-order-property-index (get instant-property-index-map highest-order-property)
            all (> day-index highest-order-property-index)]
        (if all
          [current-time false]
          (let [current-day (t/day current-time)]
            (if (> current-day 1)
              (let [time (t/plus current-time (t/months 1))]
                [(t/date-time (t/year time)
                              (t/month time)
                              1
                              0
                              0
                              0)
                 true])
              [current-time false])))))))

(defn next-month [current-time compiled-recurrence-pattern recurrence-pattern]
  (let [pattern (get compiled-recurrence-pattern :month)
        current (t/month current-time)
        next-value (next-value current pattern)
        year (t/year current-time)
        day (t/day current-time)]
    (cond
     (= next-value :roll-over) [(t/plus (t/date-time year
                                                     1
                                                     1
                                                     0
                                                     0
                                                     0)
                                        (t/years 1))
                                true]
     (< (t/day (t/last-day-of-the-month year
                                        next-value)) day) [(t/plus (t/date-time year
                                                                                next-value
                                                                                1
                                                                                0
                                                                                0
                                                                                0)
                                                                   (t/months 1))
                                                           true]
     :default [(t/date-time year
                            next-value
                            day
                            (t/hour current-time)
                            (t/minute current-time)
                            (t/second current-time))
               false])))

(defn next-year [current-time compiled-recurrence-pattern recurrence-pattern]
  (let [pattern (get compiled-recurrence-pattern :year)
        current (t/year current-time)
        next-value (next-value current pattern)]
    (if (= next-value :roll-over)
      (throw (Exception. "Maximum time reached"))
      [(t/date-time next-value
                    (t/month current-time)
                    (t/day current-time)
                    (t/hour current-time)
                    (t/minute current-time)
                    (t/second current-time))
       false])))

(defn roll-forward [current-time compiled-recurrence-pattern recurrence-pattern]
  (loop [functions [next-second next-minute next-hour next-day next-month next-year]
         time current-time
         roll-over false]
    (if (empty? functions)
      [time roll-over]
      (let [f (first functions)
            [t ro] (f time compiled-recurrence-pattern recurrence-pattern)]
        (if ro
          [t ro]
          (recur (rest functions)
                 t
                 ro) ;; guaranteed false
          )))))

(defmulti next-occurrence
  (fn [current-time recurrence-patterns]
    (cond (or (nil? recurrence-patterns) (empty? recurrence-patterns)) :empty
          (sequential? recurrence-patterns) :multiple
          (map? recurrence-patterns) :single
          :else (throw (IllegalArgumentException.
                           (str "Invalid argument: " recurrence-patterns))))))

(defmethod next-occurrence :empty [current-time recurrence-pattern]
  current-time)

(defmethod next-occurrence :multiple [current-time recurrence-patterns]
  (loop [patterns recurrence-patterns
         time max-date-time]
    (if (empty? patterns)
      time
      (let [pattern (first patterns)
            t (next-occurrence current-time pattern)]
        (recur
         (rest patterns)
         (if (t/before? t time)
           t
           time))))))

(defmethod next-occurrence :single [current-time recurrence-pattern]
  (let [compiled (compile-recurrence-pattern recurrence-pattern)]
    (loop [time current-time
           keep-going true]
      (if (not keep-going)
        time
        (let [[t roll-over] (roll-forward time compiled recurrence-pattern)]
          (recur t
                 roll-over))))))

(defn to-period [interval-pattern]
  ;; this only works if interval-pattern only has one key-value pair (which should be the case)
  (case (first (keys interval-pattern))
   :year (t/years (get interval-pattern :year))
   :month (t/months (get interval-pattern :month))
   :week (t/weeks (get interval-pattern :week))
   :day (t/days (get interval-pattern :day))
   :hour (t/hours (get interval-pattern :hour))
   :minute (t/minutes (get interval-pattern :minute))
   :second (t/seconds (get interval-pattern :second))
   (throw (IllegalArgumentException. (str "Invalid interval-pattern: " interval-pattern)))))

(defn to-monday [time]
  ;; somehow clj-time (joda time) has Monday as the beginning of week
  (let [diff (- (t/day-of-week time) 1)]
    (t/minus time (t/days diff))))

(defn max-out-lower [time unit-keyword]
  (case unit-keyword
    :year (t/date-time (t/year time) 12 31 23 59 59)
    :month (let [last-day (t/last-day-of-the-month time)]
                 (t/date-time (t/year last-day) (t/month last-day) (t/day last-day) 23 59 59))
    :week (t/date-time (t/year time) (t/month time) (t/day time) 23 59 59)
    :day (t/date-time (t/year time) (t/month time) (t/day time) 23 59 59)
    :hour (t/date-time (t/year time) (t/month time) (t/day time) (t/hour time) 59 59)
    :minute (t/date-time (t/year time) (t/month time) (t/day time) (t/hour time) (t/minute time) 59)
    :second (t/plus (zero-out-millis time) (t/seconds 1))
    (throw (IllegalArgumentException. (str "Invalid unit-keyword: " unit-keyword)))))

(defn zero-out-lower [time unit-keyword]
  (case unit-keyword
    :year (t/date-time (t/year time) 1 1 0 0 0)
    :month (t/date-time (t/year time) (t/month time) 1 0 0 0)
    :week (t/date-time (t/year time) (t/month time) (t/day time) 0 0 0)
    :day (t/date-time (t/year time) (t/month time) (t/day time) 0 0 0)
    :hour (t/date-time (t/year time) (t/month time) (t/day time) (t/hour time) 0 0)
    :minute (t/date-time (t/year time) (t/month time) (t/day time) (t/hour time) (t/minute time) 0)
    :second (zero-out-millis time)
    (throw (IllegalArgumentException. (str "Invalid unit-keyword: " unit-keyword)))))

(defn next-interval
  ([current-time schedule start-time]
     (next-interval current-time schedule start-time max-date-time))
  ([current-time schedule start-time end-time]
     (let [interval-pattern (get schedule :every)
           recurrence-pattern (get schedule :at)
           first-hit (next-occurrence start-time recurrence-pattern)]
       (if (t/after? first-hit current-time)
         first-hit
         (let [unit-keyword (first (keys interval-pattern)) ;; works only when (= 1 (count interval-pattern))
               adjusted-start-time (zero-out-lower start-time unit-keyword)
               next-one (first (filter #(t/after? % current-time)
                                       (tp/periodic-seq adjusted-start-time
                                                        (to-period interval-pattern))))]
           next-one)))))

(defn next-time
  ([current-time schedule]
     (let [start-time min-date-time
           end-time max-date-time]
       (next-time current-time schedule start-time end-time)))
  
  ([current-time schedule start-time]
     (next-time current-time schedule start-time max-date-time))
  
  ([current-time schedule start-time end-time]
     (let [current-time (zero-out-millis current-time)
           start-time (if (nil? start-time)
                        min-date-time
                        (zero-out-millis start-time))
           end-time (if (nil? end-time)
                      max-date-time
                      (zero-out-millis end-time))
           recurrence (get schedule :at)
           boundaries (get schedule :between)
           interval (get schedule :every)]
       (loop [time (t/plus current-time (t/seconds 1))]
         (if (or (t/after? time end-time)
                 (t/after? time max-date-time))
           nil
           (let [t1 (if (nil? interval)
                      time
                      (next-interval time schedule start-time end-time))
                 next-time (next-occurrence t1 recurrence)]
             (if (or (t/after? next-time end-time)
                     (t/after? next-time max-date-time))
               nil
               (if (included? next-time boundaries)
                 next-time
                 (recur (next-included-time next-time boundaries))))))))))

(defn next-n-times
  ([current-time schedule num-times]
     (let [start-time min-date-time]
       (next-n-times current-time schedule num-times start-time max-date-time)))
  ([current-time schedule num-times start-time]
     (next-n-times current-time schedule num-times start-time max-date-time))
  ([current-time schedule num-times start-time end-time]
     (loop [n num-times
            fire-times []
            current current-time]
       #_(println :n n :fire-times fire-times :current current)
       (if (= 0 n)
         fire-times
         (let [next-time (next-time current schedule start-time end-time)]
           (if (nil? next-time)
             fire-times
             (recur
              (dec n)
              (conj fire-times next-time)
              next-time)))))))
