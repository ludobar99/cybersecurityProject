const { exec } = require("child_process");

exec("mvn clean package", (error) => {
  if (error) {
    console.error(`Error: ${error}`);
  }

  console.log("Install success!");
});
