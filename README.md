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

## Usage

### Installation

Recurrence expression is available in Maven central.

If you use Maven, at this to your `pom.xml`:

```
<dependency>
  <groupId>com.bjondinc</groupId>
  <artifactId>recurrence-expression</artifactId>
  <version>0.1.0</version>
</dependency>
```

If you use Clojure and use Leiningen, add this to the `:dependencies`
section of your `project.clj`:

```
[com.bjondinc/recurrence-expression "0.1.0"]
```

Finally, if you use gradle:

```
compile 'com.bjondinc:recurrence-expression:0.1.0'
```

### Clojure

TODO

### Java

TODO

## Examples

## Rationale

## TODOs

## License

Copyright &copy; 2015 Bjönd, Inc.

This project is licensed under the [GNU Lesser General Public License v3.0][license].

[license]: http://www.gnu.org/licenses/lgpl-3.0.txt
