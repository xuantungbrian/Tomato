module.exports = {
    preset: 'ts-jest',  // Use ts-jest preset for transforming TypeScript
    testEnvironment: 'node',  // Use Node.js as the test environment
    transform: {
      '^.+\\.ts$': 'ts-jest',  // Transform TypeScript files using ts-jest
    },
    transformIgnorePatterns: [
      '/node_modules/',  // Default pattern to ignore files in node_modules
      // You can add additional patterns to avoid ignoring certain node_modules
      '<rootDir>/node_modules/some-module/',  // Example: you might need to transform this package
    ],
    moduleFileExtensions: ['ts', 'js', 'json', 'node'],
    testTimeout: 100000,
    collectCoverageFrom: [
      'controllers/*.ts',
      'errors/*.ts',
      'middleware/*ts',
      'model/*.ts',
      'service/*.ts',
      'routes/*.ts'
    ],
    setupFiles: ['<rootDir>/jest.setup.js']
  };
  