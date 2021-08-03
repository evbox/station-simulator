// Require the framework and instantiate it
const fastify = require('fastify')({
    logger: true
})

const {exec} = require('child_process');

const process = require('process');
process.chdir('../');

console.log('path',process.cwd())

// Declare a route
fastify.get('/', function (request, reply) {
    reply.send({ hello: 'world' })
})




// Run the server!
fastify.listen(4000, function (err, address) {
    if (err) {
        fastify.log.error(err)
        process.exit(1)
    }
    fastify.log.info(`server listening on ${address}`)
})



startSimulator()

function startSimulator() {
    // return new Promise(function (resolve, reject) {
        exec(
            `./gradlew run -Parguments="ws://everon.io/ocpp --configurationFile ./../configuration.yml"`,
            (err, stdout, stderr) => {
                if (err) {
                    console.log(err)

                    return;
                }


                console.log('fuu',stdout)
                // resolve(stdout.trim().replace(/\n/g, '-'));
            }
        );
    // });
}



