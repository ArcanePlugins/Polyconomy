while true
do
  read -p "READY"

  cd ..

  mvn clean install

  if [[ $? == 0 ]]
  then
    echo "Copying..."
    jarname=$(ls plugin-bukkit/target | grep "^Polyconomy")
    pathToOutJar="plugin-bukkit/target/$jarname"
    pathToTestJar="/PATH/TO/YOUR/TEST/SERVER/plugins/$jarname"

    cp -v "$pathToOutJar" "$pathToTestJar"
  else
    echo "Skipping copy; error occurred during compilation."
  fi

  cd local
done