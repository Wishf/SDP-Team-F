/*
 * CommsLib.h - Arduino library for managing communications
 * Created by SDP Group 1 2014
 */
#ifndef _COMMSLIB_H_
#define _COMMSLIB_H_

#include "Arduino.h"
#define DEBUG true

typedef void (* function_pointer_t)();

class Communications
{
    public:
        Communications();
        void set_handler(char identifier, function_pointer_t function);
        bool handle(char identifier);
        byte read_byte();
        unsigned short read_unsigned_short();
        void print(int value);
        void print(unsigned short value);
        void print(char msg[]);
        void send(byte value);
        void loop();
    private:
        bool validate_identifier(char identifier);
        function_pointer_t handlers[26];
};

#endif // _COMMSLIB_H_