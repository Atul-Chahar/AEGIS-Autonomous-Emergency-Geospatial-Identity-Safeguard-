const { ethers } = require("hardhat");

async function main() {
  console.log("Deploying AegisTouristID contract...");
  const AegisFactory = await ethers.getContractFactory("AegisTouristID");
  const aegis = await AegisFactory.deploy();

  await aegis.waitForDeployment();
  const address = await aegis.getAddress();

  console.log(`✅ AegisTouristID deployed to: ${address}`);
  console.log(`Network: ${hardhat.network.name} (Chain ID: ${hardhat.network.config.chainId || 31337})`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
