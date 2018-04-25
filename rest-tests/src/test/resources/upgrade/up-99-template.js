console.log("Starting migration to schema 99");

var path = "/integrations/:INTEGRATION_ID/";
console.log("Fetching path: " + path);

var obj=jsondb.get(path);
obj["name"]="UPGRADE INTEGRATION NAME"
jsondb.update(path, obj);

console.log("Migration to schema 99 completed");
