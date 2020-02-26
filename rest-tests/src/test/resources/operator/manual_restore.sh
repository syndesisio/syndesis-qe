#!/usr/bin/env bash

TARGET_DB_NAME=$1

base64 -d /tmp/syndesis-db.dump > /tmp/syndesis-db
psql <<< "DROP database if exists syndesis_restore"
psql <<< "CREATE database syndesis_restore"
pg_restore -v -d syndesis_restore /tmp/syndesis-db
psql <<< "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$TARGET_DB_NAME'"
psql <<< "DROP database if exists $TARGET_DB_NAME"
psql <<< "ALTER database syndesis_restore rename to $TARGET_DB_NAME"
