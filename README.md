# Recurrence Expression

This library defines a JSON schema for expressing recurrence
patterns.  It is a Java library written in Clojure.

A recurrence pattern is a pattern of date and time that you establish when
you say for example, "Let's meet every Tuesday at 9:30am".  A recurrence 
expression lets you specify such patterns in [JSON](http://json.org).

Here is an example of a recurrence expression:


```json
{ "every": { "second": 10 } }
```

The above expression means "every 10 seconds", which is a rather trivial
pattern.  Here's an example that is a bit more complex:


```json
{
  "between": {
    "from": { "hour": 9, "minute": 30 },
    "to": { "hour": 5, "minute": 30 }
  },
  "at": {
    "minute": [ 0, 15, 30, 45 ]
  }
}
```

The above expression means "every 15 minutes between 9:30am and 5:30pm, on
every day".

Several functions are included in this library to validate and
interpret recurrence expressions.  With these functions you can do
the following:

1. Validate: Is this JSON a legal recurrence expression?  (Status: not fully
   implemented yet.)
2. Calculate: Given a recurrence expression and time `t`, what is the next
   occurrence of the expression after time `t`?
3. Match: Does this time match this recurrence expression?
   (Status: will be available very soon.)

This project is written in Clojure, but we aim to serve the entire JVM
community.  We provide a wrapper Java class to that end.  Most
functions are exposed through this wrapper class.

There is also a sister library called "recurrence-trigger" that
provides a custom trigger for the
[Quartz Scheduler](http://quartz-scheduler.org).  This Quartz trigger
uses recurrence expressions.  (Status: we will publish
"recurrence-trigger" soon.)

The design goal of recurrence expressions is to makes it simple to say
simple things while still making it possible to say complex things.
We welcome your feedback.  Let us know if you have a real-life
recurrence pattern you can describe in English but is not supported by
the recurrence expression syntax.  We will review it and try to accommodate
it.  (Status: contact info pending.)

Our sincere thanks for checking out this library.

## Rationale

This project started out as a custom trigger for the Quartz Scheduler.
Quartz comes with a handful of built-in triggers, but they did not
satisfy all our scheduling needs.  So we decided to roll our own.  We
used Clojure to implement the date calculation, and as a result it was
easy to separate this project from the parts that interface with
Quartz.

We aren't the absolute authority on schedulers and recurrences.
However, we are aware of three main exemplars in computer software for
specifying recurrence patterns:

1. [Crontab for Cron](http://crontab.org)
2. [RecurrencePattern in Microsoft Outlook Calendar](https://msdn.microsoft.com/en-us/library/microsoft.office.interop.outlook.recurrencepattern(v=office.15).aspx).
3. [Recurrence Rule in RFC 2445 (iCal)](https://www.ietf.org/rfc/rfc2445.txt)

All scheduling software tools and libraries seem similar to or draw ideas
from one of above examplars.

Of the above three items, RFC 2445 offers the most comprehensive way
to specify recurrence patterns.  However, we could not find a usable
implementation of it.  We have seen one or two implementations of the
recurrence rule of RFC 2445, but none of them implements the
specification entirely.  A notable example of this is
[google-rfc-2445](https://code.google.com/p/google-rfc-2445/), which
does not support recurrence patterns finer than daily frequencies.

Both Cron and Outlook have a lot of happy users.  However, they are
not very comprehensive.  There are some patterns that can be expressed
in Outlook but not in Cron, and vice versa.  RFC 2445's recurrence
rules subsume what can be expressed by both Cron and Outlook in that
they can express patterns that neither Cron nor Outlook can express.

Recurrence expressions, like RFC 2445's recurrence rules, are a
superset of Cron expressions and Outlook's recurrence patterns.
Recurrence expressions and recurrence rules are very similar despite
the obvious syntactical differences.

So why didn't we just implement RFC 2445?  To be honest, we wrote the
code before we even knew about RFC 2445.  But we also believe our
choice of JSON as the underlying syntax for recurrence expressions was
a good one.  JSON is far easier to manipulate programmatically for
both the producer and the consumer.  We do believe there should be
interoperability between recurrence expressions and RFC 2445.  No
promises, but we plan to translation between recurrence rules and
recurrence expressions in the future.

## Usage

### Installation

Recurrence Expression is available in Maven central.

If you use Maven, add this to your `pom.xml`:


```xml
<dependency>
  <groupId>com.bjondinc</groupId>
  <artifactId>recurrence-expression</artifactId>
  <version>0.1.1</version>
</dependency>
```

If you use Clojure and Leiningen, add this to the `:dependencies`
section of your `project.clj`:


```clojure
[com.bjondinc/recurrence-expression "0.1.1"]
```

Finally if you use gradle, add this under `dependencies`:


```gradle
compile 'com.bjondinc:recurrence-expression:0.1.1'
```

### Java

Recurrence Expression relies on
[Joda-Time](http://www.joda.org/joda-time/) for date-time calculations
and requires you to use Joda-Time's `DateTime` object to specify a
point in time.

```java
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import com.bjondinc.RecurrenceExpression;

public class RecurrenceExpressionTests {

	@Test
	public void testNextTime() {
		
		/*
		 * The expression below says "Every 5 minutes, at 30 seconds".
		 * The start time is set to 6/9/2015 at 4pm.  
		 * 
		 * The expression and the start time evaluate to the following sequence:
		 * 
		 *   [ 16:05:30, 16:10:30, 16:15:30, ... ]
		 * 
		 */
		String expression = "{ \"every\": { \"minute\": 5 }, \"at\": { \"second\": 30 } }";
		DateTime startTime = new DateTime(2015, 6, 9, 16, 0, 0, DateTimeZone.UTC);

		/*
		 * Given the current time of 6/9/2015 at 4:43:15pm, ...
		 */
		DateTime currentTime = new DateTime(2015, 6, 9, 16, 43, 15, DateTimeZone.UTC);

		/*
		 * ... what is the next time in the sequence?
		 */
		DateTime actual = RecurrenceExpression.nextTime(currentTime, expression, startTime);
		
		/*
		 * It should be 4:45:30pm.
		 */
		DateTime expected = new DateTime(2015, 6, 9, 16, 45, 30, DateTimeZone.UTC);
		
		assertEquals(expected, actual);
	}
}
```

### Clojure

Recurrence Expression uses
[clj-time](https://github.com/clj-time/clj-time) internally and
currently requires you to use clj-time's `date-time` to specify a
point in time.


```clojure
;; load libraries
user> (require '[clj-time.core :as t])
user> (require '[recurrence-expression.core :as rc])

;; ":every" clause requires a start-time.
user> (def start-time (t/date-time 2015 03 14))
#'user/start-time
user> start-time
#<DateTime 2015-03-14T00:00:00.000Z>

user> (rc/next-time (t/date-time 2015 03 14 9 26 53) {:every {:second 10}} start-time)
#<DateTime 2015-03-14T09:27:00.000Z>
user>
```

In the above example, we use a Clojure map to represent a recurrence
expression. (Specifically: `{:every {:second 10}}`.) It is because we use
a Clojure data structure internally when performing calculations.

## Example Expressions

TODO

## License

Copyright &copy; 2015 Bjönd, Inc.

This project is licensed under the [GNU Lesser General Public License v3.0][license].

[license]: http://www.gnu.org/licenses/lgpl-3.0.txt
