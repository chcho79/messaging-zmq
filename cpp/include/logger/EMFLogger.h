
#ifndef EMF_LOGGER_H_
#define EMF_LOGGER_H_

#include <stdio.h>
#include <stdarg.h>
#ifdef __cplusplus
#include <cinttypes>
#else
#include <inttypes.h>
#endif

#ifndef UNUSED
#define UNUSED(x) (void)(x)
#endif

// Max buffer size
#define MAX_LOG_V_BUFFER_SIZE (256)

// Setting this flag for a log level means that the corresponding log message
// contains private data. This kind of message is logged only when a call to
// EMFSetLogLevel() enabled private data logging.
#define EMF_LOG_PRIVATE_DATA (1 << 31)

typedef enum {
    DEBUG = 0,
    INFO,
    WARNING,
    ERROR,
    FATAL
} LogLevel;

#define DEBUG_PRIVATE ((EMF_LOG_PRIVATE_DATA) | (DEBUG))
#define INFO_PRIVATE ((EMF_LOG_PRIVATE_DATA) | (INFO))
#define WARNING_PRIVATE ((EMF_LOG_PRIVATE_DATA) | (WARNING))
#define ERROR_PRIVATE ((EMF_LOG_PRIVATE_DATA) | (ERROR))
#define FATAL_PRIVATE ((EMF_LOG_PRIVATE_DATA) | (FATAL))

#ifndef EMF_LOG_LEVEL
#define EMF_MINIMUM_LOG_LEVEL    (DEBUG)
#else
#define EMF_MINIMUM_LOG_LEVEL    (EMF_LOG_LEVEL)
#endif

#define IF_EMF_PRINT_LOG_LEVEL(level) \
    if (((int)EMF_MINIMUM_LOG_LEVEL) <= ((int)(level & (~EMF_LOG_PRIVATE_DATA))))

/**
 * Set log level and privacy log to print.
 *
 * @param level - log level.
 * @param hidePrivateLogEntries - Hide Private Log.
 */
void EMFSetLogLevel(LogLevel level, bool hidePrivateLogEntries);

/**
* Output a variable argument list log string with the specified priority level.
*
* @param level  - DEBUG, INFO, WARNING, ERROR, FATAL
* @param tag    - Module name
* @param format - variadic log string
*/
void EMFLogv(int level, const char * tag, const char * format, ...)
#if defined(__GNUC__)
    __attribute__ ((format(printf, 3, 4)))
#endif
    ;

/**
* Output a log string with the specified priority level.
*
* @param level  - DEBUG, INFO, WARNING, ERROR, FATAL
* @param tag    - Module name
* @param logStr - log string
*/
void EMFLog(int level, const char * tag, const char * logStr);

/**
* Output the contents of the specified buffer (in hex) with the specified priority level.
*
* @param level      - DEBUG, INFO, WARNING, ERROR, FATAL
* @param tag        - Module name
* @param buffer     - pointer to buffer of bytes
* @param bufferSize - max number of byte in buffer
*/
void EMFLogBuffer(int level, const char* tag, const uint8_t* buffer, size_t bufferSize);


#ifdef DEBUG_LOG

#define EMF_LOG_BUFFER(level, tag, buffer, bufferSize) \
    do { \
        IF_EMF_PRINT_LOG_LEVEL((level)) \
            EMFLogBuffer((level), (tag), (buffer), (bufferSize)); \
    } while(0)


#define EMF_LOG(level, tag, logStr) \
    do { \
        IF_EMF_PRINT_LOG_LEVEL((level)) \
            EMFLog((level), (tag), (logStr)); \
    } while(0)

// Define variable argument log function
#define EMF_LOG_V(level, tag, ...) \
    do { \
        IF_EMF_PRINT_LOG_LEVEL((level)) \
            EMFLogv((level), (tag), __VA_ARGS__); \
    } while(0)

#define EMF_SCOPE_LOGGER(TAG, FUNC) ScopeLogger scopeLogger(TAG, FUNC)

#else // DEBUG_LOG
#define EMF_LOG(level, tag, logStr)
#define EMF_LOG_V(level, tag, ...)
#define EMF_LOG_BUFFER(level, tag, buffer, bufferSize)
#define EMF_SCOPE_LOGGER(TAG, FUNC)
#endif // DEBUG_LOG

 namespace emf
{
    class ScopeLogger
    {
        public:
            ScopeLogger(const char *tag, const char *method)
            {
                m_funName = method;
                m_tag = tag;
                EMF_LOG_V(DEBUG, m_tag, "[%s] IN", m_funName);
            }

            ~ScopeLogger()
            {
                EMF_LOG_V(DEBUG, m_tag, "[%s] OUT", m_funName);
            }

        private:
            const char *m_funName;
            const char* m_tag;
    };
}

#endif // EMF_LOGGER_H_
