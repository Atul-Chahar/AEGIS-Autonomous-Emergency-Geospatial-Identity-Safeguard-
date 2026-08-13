const hazardRepository = require('../repositories/HazardRepository');

class HazardService {
  async processHazardReport(data) {
    const result = await hazardRepository.createHazardReport(data);
    return result;
  }

  async listHazards() {
    return hazardRepository.getAllHazards();
  }
}

module.exports = new HazardService();
