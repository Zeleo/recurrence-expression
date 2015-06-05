# recurrence-expression

This library defines a JSON schema for expressing recurrence
patterns.  It's a Java library but written in Clojure.

Recurrence pattern is the pattern of date and time you establish when
you say, for example, "Let's meet every Tuesday at 9:30am".

Here is an example of a recurrence expression:


```json
{ "every": { "second": 10 } }
```

Above expression means "every 10 seconds".

OK, "every 10 seconds" is a rather trivial pattern.  Here's a bit more
complex example:


```json
{ "between": { "from": { "hour": 9, "minute": 30 },
               "to": { "hour": 5, "minute": 30 } },
  "at": { "minute": [ 0, 15, 30, 45 ] } }
```

Above means "Everybday between 9:30am to 5:30pm, every 15 minutes".

Several functions are included in this library to validate and
interpret recurrence expressions.  With these functions you can do
these things:

1. Validation: is this JSON a legal recurrence expression?
2. Calculation: Given a recurrence expression and time `t`, what is the next
occurrence after time `t`?
3. Matching: does this time matches this recurrence expression?

This project is written in Clojure, but we aim to serve the entire JVM
community.  We provide a wrapper Java class to that end.  Most
functions are exposed through this wrapper class.

There is also a sister library called "recurrence-trigger" that
provides a custom trigger for Quartz Scheduler.  This Quartz trigger
uses recurrence expression.

Recurrence expression aims to make it simple to say simple things
while making it possible to say complex things.  We welcome your
feedback, although we still need to figure out how to actually gather
your feedback.  We'll follow up on this shortly.  Let us know if you
have a real-life recurrence pattern you can describe in English but
not supported by recurrence expression.  We'll review it and try to
accommodate it.

Our sincere thanks for checking out this library.

## Rationale

This project started out as a custom trigger for Quartz Scheduler.
Quartz comes with a handful of built-in triggers, but they did not
satisfy all our scheduling needs.  So we decided to roll our own.  We
used Clojure to implement the date calculation, and as a result it was
easy to separate this project out from the parts that interfaces with
Quartz.

We aren't the absolute experts on schedulers and recurrences.
However, we are aware of two main exemplars in computer software for
specifying recurrence patterns:

1. [Crontab for Cron](http://crontab.org)
2. [RecurrencePattern in Microsoft Outlook Calendar](https://msdn.microsoft.com/en-us/library/microsoft.office.interop.outlook.recurrencepattern(v=office.15).aspx).

Scheduling software tools and libraries seem to use either a variant
of cron expression or follow a similar approach to Outlook recurrence
pattern.

To be sure, both Cron and Outlook have a lot of happy users.  Each
of them individually cover most recurrence patterns we want to
express.

There are, however, some edge-case patterns neither Cron or Outlook
can express.  Also, there are some patterns that can be expressed in
Outlook but not in Cron, and vice versa.

Recurrence expression is a superset of Cron expression and Outlook's
recurrence pattern.  It is also capable of expressing patterns that
neither Cron nor Outlook can express.  A bit verbose, we admit, but
it's also a lot more readable than, say, `59 11 * * 1-5`.

## Usage

### Installation

Recurrence expression is available in Maven central.

If you use Maven, add this to your `pom.xml`:


```xml
<dependency>
  <groupId>com.bjondinc</groupId>
  <artifactId>recurrence-expression</artifactId>
  <version>0.1.0</version>
</dependency>
```

If you use Clojure and use Leiningen, add this to the `:dependencies`
section of your `project.clj`:


```clojure
[com.bjondinc/recurrence-expression "0.1.0"]
```

Finally if you use gradle, add this under `dependencies`:


```gradle
compile 'com.bjondinc:recurrence-expression:0.1.0'
```

### Clojure

Recurrence-expression uses clj-time internally and currently requires you to use
clj-time's `date-time` to specify a point in time.

```clojure
;; load libraries
user> (require '[clj-time.core :as t])
user> (require '[recurrence-expression.core :as rc])

;; ":every" clause requires a start-time.
user> (def start-time (t/date-time 2015 03 14))
#'user/start-time
user> start-time
#<DateTime 2015-03-14T00:00:00.000Z>

user> (rc/next-fire-time (t/date-time 2015 03 14 9 26 53) { :every { :second 10 }} start-time)
#<DateTime 2015-03-14T09:27:00.000Z>
user>
```
### Java

TODO

## Example Expressions

TODO

## TODOs

1. Finish README.md
1. Timezone support within next-time function.
1. Fix miscellaneous defects.
   1. Make sure end-time is respected.
1. Refine interface (Joda time, clj-time, and instance-pattern)?
1. Support roll-over.
1. Consider switching to Hubert for schema validation.
1. Add test.check tests.
1. Consider switching to cheshire for JSON<-->Clojure translation.
1. Support calendar.

## License

Copyright &copy; 2015 Bjönd, Inc.

This project is licensed under the [GNU Lesser General Public License v3.0][license].

[license]: http://www.gnu.org/licenses/lgpl-3.0.txt
