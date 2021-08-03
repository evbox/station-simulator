let startBtn = document.getElementById('start');
let stopBtn = document.getElementById('stop');
let stateBtn = document.getElementById('state');

startBtn.addEventListener('click', (e) => {
    e.preventDefault();

    window.api.start('STARTTTTT')
});


stopBtn.addEventListener('click', (e) => {
    e.preventDefault();

    window.api.stop('STOPPPPPL')
});

stateBtn.addEventListener('click', (e) => {
    e.preventDefault();

    window.api.state()

});
