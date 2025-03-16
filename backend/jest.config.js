module.exports = {
    preset: 'ts-jest', 
    testEnvironment: 'node',
    transform: {
      '^.+\\.tsx?$': 'ts-jest',
    },
    transformIgnorePatterns: [
      '/node_modules/', 
      '<rootDir>/node_modules/some-module/',
    ],
    moduleFileExtensions: ['ts', 'js', 'json', 'node'],
    testTimeout: 50000,
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
  