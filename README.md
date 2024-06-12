# id_builder

Generates globally unique long numbers based on timestamp and host id.

Two thread safe implementations available:

* DecimalIdGenerator - keeps id elements in decimal positions of a 19-digit long number; Human readable
* BinaryIdGenerator - keeps id elements in binary bits.
