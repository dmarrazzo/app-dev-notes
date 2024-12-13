from('timer:tick?period=4000')
  .setBody().constant('Hello world from Camel K')
  .to('log:info')
