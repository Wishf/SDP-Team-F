#include "Arduino.h"
#include "CommsLib.h"

Communications::Communications()
{
    // Make sure all handlers default to NULL
    for(int i = 0; i < 26; i++){
        handlers[i] = NULL;
    }
}

bool Communications::validate_identifier(char identifier)
{
    // Remove offset (ASCII 'A' is 65)
    byte index = identifier - 65;

    // Check if the index is within the bounds of the array
    return !(index < 0 || index >= 26);
}

void Communications::set_handler(char identifier, function_pointer_t function)
{
    // Check the identifier is valid
    if (!validate_identifier(identifier))
        return;

    // Resolve index as in validate_identifier and assign handler
    identifier = identifier - 65;

    handlers[identifier] = function;
}

bool Communications::handle(char identifier)
{
    // Check the identifier is valid
    if(!validate_identifier(identifier))
        return false;

    // Resolve index as in validate_identifier and if a handler exists, handle it
    identifier = identifier - 65;

    if(handlers[identifier] != NULL)
        handlers[identifier]();
        return true;

    return false;
}

byte Communications::read_byte()
{
    // Loop until a byte is available then read it
    while(!Serial.available());
    return Serial.read();
}

unsigned short Communications::read_unsigned_short()
{
    // Read bytes from communications for top and bottom halfs
    byte top = read_byte();
    byte bottom = read_byte();

    // Shift top byte up by 8 bits and boolean OR with bottom byte to produce 16 bit value
    return (top << 8) | bottom;
}

void Communications::print(int value)
{
    if(DEBUG){
        Serial.print(value, DEC);
    }
}

void Communications::print(unsigned short value)
{
    if(DEBUG){
        Serial.print(value, HEX);
    }
}

void Communications::print(char msg[])
{
    if(DEBUG){
        Serial.print(msg);
    }
}

void Communications::send(char value)
{
    Serial.print(value);
}

void Communications::loop()
{
    // If we have bytes waiting, read them and attempt to handle
    if(Serial.available()){
        byte incoming = Serial.read();

        // If handling fails, send error code and debug message if enabled
        if(!handle(incoming)){
          send('E');

          print("get off my lawn");
        }
    }
}