# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased - planned]
- patch updates of extractors should not break/invalidate existing camel routes

## [3.0.4] - INPROGRESS
* extend route status response, add info for each extractor

## [3.0.3] - 2016-09-26 
#### Added
- load camel routes from external package during startup (if available)

#### Changed
- harmonize json response of createItem() and addPart()

## [3.0.2] - 2016-08-26

#### Added
* add predefined workflows for animaldetection -yolo and -dpm

#### Changed
* adapt uri of mico-registration-service

## [3.0.1] - 2016-08-05

#### Added
- web page for direct inject to workflows
- syntactic type information to createItem response

#### Changed
- short workflow description for camel routes, used during inject

## [3.0.0] - 2016-07-20

#### Added
- support for MICO metadata model 2.0

#### Changed
- creation of new item can include a binary upload

#### Removed
- support of MICO metadata model 1.x