console.log("Starting migration to schema 98");

var path = "/integrations/:INTEGRATION_ID/";
console.log("Fetching path: " + path);

var obj=jsondb.get(path);
obj["description"]="UPGRADE INTEGRATION DESCRIPTION"
jsondb.update(path, obj);

console.log("Migration to schema 98 completed");
