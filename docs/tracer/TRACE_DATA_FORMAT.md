
# Strings, Keywords, Symbols, Methods, Traces




# Raw trace data

Raw trace is a data stream produced directly by tracer. This format is optimized for agents in order to minimize 
agent overhead but in doing so it requires some post processing in collector.  

Each trace is encoded as variable-length array containing information on current method call:

```
(TAG=10/11)[prolog,begin?attr1?,attr2?,...,sub1,sub2...,attrn?,attrn+1?,...,excepption?,epilog]
```

Both prolog and epilog contain data in architecture dependent byte order. If `TAG=6` this is big endian, for `TAG=7`
it's little endian (most popular architectures, notably x86).

Prolog is a CBOR byte array that contains one 64-bit word containing following data:

    0                  40          64
    +-------------------+-----------+
    |tstamp (call start)|method_id  |
    +-------------------+-----------+

* `tstamp` - is number of ticks since JVM start; a tick is calculated as `System.nanoTime() >> 16` (2^16 ns);

* `method_id` - method ID as assigned when instrumenting class;


Epilog is a CBOR byte array that contains one 64-bit word (with optional second word) containing following data:

    0                  40          64                              128
    +-------------------+-----------+-------------------------------+
    |tstamp (call end)  |calls      | long_calls (optional)         |
    +-------------------+-----------+-------------------------------+

* `tstamp` - is number of ticks since JVM start; a tick is calculated as `System.nanoTime() >> 16` (2^16 ns);

* `calls` - number of (instrumented) method calls (1 if no methods were called from current method); 
if this number exceeds 2^24-1, it is set to 0 and 64-bit variant is appended (`long_calls`);


After prolog there can be arbitrary number of attribute maps or subordinate trace records. Also, exception can be 
added if thrown by method. Trace can mix arbitrary 

## TraceBegin

TraceBegin structure marks beginning of a trace. 

```
(TAG=33)[clock,traceId]
```

* `clock` - current time (as from `System.currentTimeMillis()`);

* `traceId` - trace ID (as an integer);


## Attributes

```
(TAG=12){key1,val1,key2,val2,...keyN,valN}
```

Where both keys and values can be of arbitrary (possibly recursive) type. Note that both keys or value can be or 
contain string-refs and similar types.


## Exceptions

Full exception info is encoded as tagged 5-element array:

```
(TAG=34)[id,class,message,stack,cause]
```

* `id` - (not certainly unique ID) result of `System.identityHashCode(e)`; 

* `class` - exception class name (string or string-ref);

* `message` - exception message (string);

* `stack` - array of stack trace elements;

Each stack element is encoded as 4-element array:

```
[class,method,file,line]
```
 
* `class` - class name (string ref);

* `method` - method name (string ref);

* `file` - file name (string ref);

* `line` - line number (integer);


