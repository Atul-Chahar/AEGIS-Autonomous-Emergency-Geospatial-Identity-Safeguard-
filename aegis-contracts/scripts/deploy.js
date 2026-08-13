const hre = require("hardhat");

async function main() {
  console.log("🛡️ Deploying AEGIS Privacy Tourist ID Smart Contract...");

  const AegisTouristID = await hre.ethers.getContractFactory("AegisTouristID");
  const aegisContract = await AegisTouristID.deploy();

  await aegisContract.waitForDeployment();

  const contractAddress = await aegisContract.getAddress();
  console.log(`✅ AegisTouristID deployed to: ${contractAddress}`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
