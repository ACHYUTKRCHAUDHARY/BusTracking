FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Vite bakes VITE_* vars into the bundle at build time, so this must be a
# build ARG (set via Railway's build-time variables), not a runtime env var.
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
# nginx's official entrypoint envsubst's *.template files in /etc/nginx/templates/
# into /etc/nginx/conf.d/ on container start, so $PORT (set by Railway at
# runtime) can be baked into the listen directive.
COPY nginx.conf /etc/nginx/templates/default.conf.template

ENV PORT=80
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
