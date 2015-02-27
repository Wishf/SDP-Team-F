#include "Arduino.h"
#include "CommsLib.h"

Communications::Communications()
{
    for(int i = 0; i < 26; i++){
        handlers[i] = NULL;
    }
}

bool Communications::validate_identifier(char identifier)
{
    byte index = identifier - 65;

    return !(index < 0 || index >= 26);
}

void Communications::set_handler(char identifier, function_pointer_t function)
{
    if (!validate_identifier(identifier))
        return;

    identifier = identifier - 65;

    handlers[identifier] = function;
}

bool Communications::handle(char identifier)
{
    if(!validate_identifier(identifier))
        return false;

    identifier = identifier - 65;

    if(handlers[identifier] != NULL)
        handlers[identifier]();
    return true;
}

byte Communications::read_byte()
{
    while(!Serial.available());
    return Serial.read();
}

unsigned short Communications::read_unsigned_short()
{
    byte top = read_byte();
    byte bottom = read_byte();

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
    if(Serial.available()){
        byte incoming = Serial.read();

        if(!handle(incoming)){
          send('E');

          print("get off my lawn");
        }
    }
}