
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <log.h>
#include "face_db.h"

int open_db(const char *db_path, sqlite3 **db) {
    char *zErrMsg = 0;
    int  rc;
    char *sql;

    // Open Database
    rc = sqlite3_open(db_path, db);
    if (rc) {
        fprintf(stderr, "Can't open database: %s\n", sqlite3_errmsg(*db));
        return -1;
    } else {
        fprintf(stdout, "Opened database successfully\n");
    }

    // Create FACE Table
    sql = "CREATE TABLE IF NOT EXISTS FACE ("  \
        "ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," \
        "NAME TEXT," \
        "INFO TEXT," \
        "FEATURE_SIZE INT," \
        "FEATURE BLOB);";

    rc = sqlite3_exec(*db, sql, NULL, 0, &zErrMsg);
    if (rc != SQLITE_OK) {
        fprintf(stderr, "SQL error: %s\n", zErrMsg);
        sqlite3_free(zErrMsg);
        return -1;
    }
    sqlite3_free(zErrMsg);

    return 0;
}

int close_db(sqlite3 *db) {
    sqlite3_close(db);
    return 0;
}

int get_face_count(sqlite3* db, int *face_num) {
    char *sql = "SELECT COUNT(*) FROM FACE;";
    sqlite3_stmt *stmt;

    int count = 0;
    char *zErrMsg = 0;
    int rc;

    rc = sqlite3_prepare_v2(db, sql, -1, &stmt, NULL);
    if (rc != SQLITE_OK) {
        fprintf(stderr, "error sqlite3_prepare_v2 %d\n", rc);
        return -1;
    }
    rc = sqlite3_step(stmt);
    if (rc != SQLITE_ROW) {
        fprintf(stderr, "error sqlite3_step: %d\n", rc);
        return -1;
    }
    count = sqlite3_column_int(stmt, 0);

    *face_num = count;

    sqlite3_finalize(stmt);

    return 0;
}

int get_all_face(sqlite3* db, face_db_record_t* face_array, int *face_num) {
    char *sql = "SELECT * FROM FACE;";

    sqlite3_stmt *stmt;

    sqlite3_prepare(db, sql, strlen(sql), &stmt, 0);

    int result = sqlite3_step(stmt);

    int index = 0;
    while (result == SQLITE_ROW) {
        const unsigned char *name = sqlite3_column_text(stmt, 1);
        const unsigned char *info = sqlite3_column_text(stmt, 2);
        int feature_len = sqlite3_column_int(stmt, 3);
        const void* feature = sqlite3_column_blob(stmt, 4);
        strncpy(face_array[index].id, (const char *)name, FACE_ID_MAX_SIZE);
        strncpy(face_array[index].info, (const char *)name, FACE_INFO_MAX_SIZE);
        face_array[index].size = feature_len;
        face_array[index].feature = malloc(feature_len);
        memcpy(face_array[index].feature, feature, feature_len);
        
        index++;
        result = sqlite3_step(stmt);
    }

    sqlite3_finalize(stmt);

    return 0;
}

int insert_face(sqlite3* db, face_db_record_t* face) {
    char sql[512] = {0};

    int rc = 0;

    sqlite3_stmt *stmt;
    int feature_index;

    snprintf(sql, 512, "INSERT INTO FACE (NAME, INFO, FEATURE_SIZE, FEATURE) VALUES ('%s', '%s', %d, ? );", 
        face->id, face->info, face->size);
 
    // printf("sql: %s\n", sql);

    rc = sqlite3_prepare(db, sql, strlen(sql), &stmt, 0);
    if (rc != SQLITE_OK) {
        fprintf(stderr, "sqlite3_prepare error: %d\n", rc);
        goto error;
    }

    rc = sqlite3_bind_blob(stmt, 1, face->feature, face->size, NULL);
    if (rc != SQLITE_OK) {
        fprintf(stderr, "sqlite3_bind_blob error: %d\n", rc);
        goto error;
    }

    sqlite3_step(stmt);

error:
    sqlite3_finalize(stmt);

    return rc;
}

int delete_face(sqlite3* db, const char* face_id) {
    int ret = 0;
    char *zErrMsg = NULL;
    char sql[512] = {0};
    int rc = 0;

    char _face_token[FACE_ID_MAX_SIZE+1];
    memset(_face_token, 0, FACE_ID_MAX_SIZE+1);
    memcpy(_face_token, face_id, FACE_ID_MAX_SIZE);

    snprintf(sql, 512, "DELETE FROM %s WHERE FACE_TOKEN='%s';", "FACE", _face_token);

    LOGD("sql: %s\n", sql);

    rc = sqlite3_exec(db, sql, NULL, 0, &zErrMsg);
    if (rc != SQLITE_OK) {
        LOGE("sqlite3_exec %s error: %d %s\n", sql, rc, zErrMsg);
        ret = -1;
    }

    error:
    if (zErrMsg != NULL) {
        sqlite3_free(zErrMsg);
    }

    return ret;
}

int release_face_data(face_db_record_t* face_array, int face_num) {
    for (int i = 0; i < face_num; i++) {
        if (face_array[i].feature != NULL) {
            free(face_array[i].feature);
        }
    }
    return 0;
}