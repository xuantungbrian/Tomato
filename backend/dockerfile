ARG PORT

FROM node:18.12.1-alpine

WORKDIR /app

COPY package.json ./

COPY package-lock.json ./

RUN npm ci

COPY . .

EXPOSE $PORT

CMD ["npm", "start"]