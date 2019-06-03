# meteorology-QA-project

## Project description

Project to carry out the consultation of the weather forecast for the next few days, for a given area. This project was developed within the scope of Quality Assurance and Testing course.

## Structure

* A minimalist web page that lets you enter or select a location and refer to its prediction;
* An API (REST) that can be invoked by external clients. The API allows you to programmatically interrogate the weather forecast for different locations.
* Integration with external source. Actual weather forecast data must be obtained from remote services. That is, the weather forecasting service itself must act as a client (from a third-party API) to obtain the values from the Internet.

## Technologies used

### Implementation

* Web: HTML, JS and JQuery
* Backend: SpringBoot

### Testing

* Unit tests: JUnit4
* Isolation of dependencies with mocks: JUnit4 + Mockito
* API integration tests: REST-Assured
* Web function tests: Selenium WebDriver
