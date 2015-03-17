/*
 * CommsLib.h - Arduino library for managing communications
 * Created by SDP Group 1 2014
 */
#ifndef _COMMSLIB_H_
#define _COMMSLIB_H_

#include "Arduino.h"
#define DEBUG true

typedef void (* function_pointer_t)();

/*
 * This class represents a communications manager which can handle up to 26 uniquely identified packet types.
 * Each packet is identified by a capital letter as it's header
 */
class Communications
{
    public:
        /*
         * Constructs a new instance of the communications manager
         */
        Communications();

        /*
         * Registers a new handler with the communications manager
         *
         * PARAMETERS
         * identifier (char) - The capital letter that identifies the packet
         * function (function_pointer_t) - The function to call when a packet of that type is recieved
         */
        void set_handler(char identifier, function_pointer_t function);

        /*
         * Given a packet identifier, call the associated handler for that identifier
         *
         * PARAMETERS
         * identifier (char) - The packet identifier
         *
         * RETURNS
         * False if the packet could not be handled (no handler or invalid identifier)
         * True if the packet was successfully handled
         */
        bool handle(char identifier);

        /*
         * Reads a single byte from the communications channel
         *
         * RETURNS
         * The next received byte
         */
        byte read_byte();

        /*
         * Reads an unsigned short (2 byte number representing 0 to 2^16) from the communications channel
         *
         * RETURNS
         * The next recieved unsigned short
         */
        unsigned short read_unsigned_short();

        /*
         * If debugging is enabled, this prints the string representation of an integer to the communications channel
         *
         * PARAMETERS
         * value (int) - The integer value to print
         */
        void print(int value);

        /*
         * If debugging is enabled, this prints the string representation (in hexadecimal) of an unsigned short
         * to the communications channel
         *
         * PARAMETERS
         * value (unsigned short) - The value to print
         */
        void print(unsigned short value);

        /*
         * If debugging is enabled, this prints a string to the communications channel
         *
         * PARAMETERS
         * msg (char[]) - The message to print
         */
        void print(char msg[]);

        /*
         * Send a character back across the communications channel
         *
         * PARAMETERS
         * value (char) - The character to send back
         */
        void send(char value);

        /*
         * Read an identifier from the communications channel and dispatch the relevant packet handler
         */
        void loop();
    private:
        /*
         * Check if a given identifier is valid
         *
         * PARAMETERS
         * identifier (char) - The identifier to check
         *
         * RETURNS
         * True if the identifier is valid
         * False if not
         */
        bool validate_identifier(char identifier);

        /*
         * An array of alphabetically identifier packet handlers
         */
        function_pointer_t handlers[26];
};

#endif // _COMMSLIB_H_