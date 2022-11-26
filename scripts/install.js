const { exec } = require("child_process");

exec("mvn clean install package", (error) => {
  if (error) {
    console.error(`Error: ${error}`);
  }

  console.log("Install success!");
});
