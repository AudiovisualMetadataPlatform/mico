#!/bin/bash
xxd -i $1 | sed 's/ \+[a-zA-Z_]\+persistence_sparql_/ /' > $2
