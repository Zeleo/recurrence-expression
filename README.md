# recurrence-expression

This library defines a Clojure object schema for expressing recurrence
patterns.  For example,

```
{ :every { :second 10 } }
```
Above expresses what we ordinarily mean by "every 10 seconds".  OK, "Every
10 seconds" is a rather trivial pattern.  Recurrence expression allows
for expressing more complex patterns.  See more examples in the Usage
section below.

Several functions are included in this library to validate and
interpret recurrence expressions.  These functions answer the
following types of questions:

1. Is this object a recurrence expression?
2. Given a recurrence expression and time `t`, what is the next
occurrence after time `t`?
3. etc, etc...

Although this project is written in Clojure, our aim is to serve the
entire JVM community.  To facilitate adoption, this library contains a
Java class that exposes the functions provided by the library.
If you use the Java class, you can write recurrence expression in
JSON.  Like this:

```
{ 'every': { 'second': 10 } }
```
There is also a sister library called "recurrence-trigger" that
provides a custom trigger for Quartz Scheduler.  This custom trigger
is based on recurrence expression.

Recurrence expression aims to make it simple to say simple things,
while making it possible to say complex things.  We welcome your
feedback, although we still need to figure out how to gather your
feedback.  We'll follow up.  Let us know if you have a practical
recurrence pattern you can describe in English but not supported by
recurrence expression.  We'll review it and try to accommodate it.

Our thanks for looking.

## Rationale

This project started out as a calculation engine to implement a custom trigger
for Quartz Scheduler.  Although Quartz comes with a handful of
built-in triggers, it's impossible to compose single trigger that
satisfies all your scheduling needs.  So we decided to roll our own.

We admit we aren't the absolute expert on recurrence patterns.  But ~~googling~~
researching a bit on the subject, we dug out the following relevant
software:

1. [Cron](http://en.wikipedia.org/wiki/Cron)
1. [Microsoft Outlook RecurrencePattern](https://msdn.microsoft.com/en-us/library/microsoft.office.interop.outlook.recurrencepattern(v=office.15).aspx)
1. Several Javascript libraries, which are similar to Outlook's
RecurrencePattern.

The venerable cron is surprisingly deficient in expressing recurrence
patterns we use to arrange meetings, for example.  Microsoft Outlook
is pretty good, but it's not very flexible and lacks where cron
shines.  With recurrence-expression we hope to cover both grounds and
more.

## Usage

### Installation

Recurrence expression is available in Maven central.

If you use Maven, at this to your `pom.xml`:

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
1. Fix miscellaneous defects.
1. Refine interface.
1. Consider switching to Hubert for schema validation.
1. Add test.check tests.

## License

Copyright &copy; 2015 Bjönd, Inc.

This project is licensed under the [GNU Lesser General Public License v3.0][license].

[license]: http://www.gnu.org/licenses/lgpl-3.0.txt
