/*Template*/

//Servicio del cliente que estará accesible desde la red

#include "calculadora.h"
#include <stdio.h>

/*Se define la función Calculadora_prog_1, como parámetros se requiere
obtener la IP a la cuál nos vamos a conectar y las variables con las que
se harán las operaciones*/
void
calculadora_prog_1(char *host, int a, int b) 
{
	CLIENT *clnt; //Se define un objeto de tipo cliente
	int  *result_1;
	dupla_int  suma_1_arg;
	int  *result_2;
	dupla_int  resta_1_arg;
	int  *result_3;
	dupla_int  division_1_arg;
	int  *result_4;
	dupla_int  multiplicacion_1_arg;

#ifndef	DEBUG
/*Se crea el cliente y éstas lineas de código son para conectarnos al servidor*/

	clnt = clnt_create (host, CALCULADORA_PROG, CALCULADORA_PROG_VERS, "udp");
//Se indica una sentencia en caso de que la llamada no se haya establecido
	if (clnt == NULL) {
		clnt_pcreateerror (host);
		exit (1);
	}
#endif	/* DEBUG */

/*En lugar de igualar a un número respectivamente cada operación se le asigna la variable que contendrá
el número */

	suma_1_arg.a=a;
	suma_1_arg.b=b;

	resta_1_arg.a=a;
	resta_1_arg.b=b;
	
	division_1_arg.a=a;
	division_1_arg.b=b;

	multiplicacion_1_arg.a=a;
	multiplicacion_1_arg.b=b;

	result_1 = suma_1(&suma_1_arg, clnt);
	if (result_1 == (int *) NULL) {
		clnt_perror (clnt, "call failed");
	}else{
		printf("result SUMA = %d\n",*result_1);
	}

	result_2 = resta_1(&resta_1_arg, clnt);
	if (result_2 == (int *) NULL) {
		clnt_perror (clnt, "call failed");
	}else{
		printf("result RESTA = %d\n",*result_2);
	}

	result_3 = division_1(&division_1_arg, clnt);
	if (result_3 == (int *) NULL) {
		clnt_perror (clnt, "call failed");
	}else{
		printf("result DIVISION = %d\n",*result_3);
	}

	result_4 = multiplicacion_1(&multiplicacion_1_arg, clnt);
	if (result_4 == (int *) NULL) {
		clnt_perror (clnt, "call failed");
	}else{
		printf("result MULTIPLICACION = %d\n",*result_4);
	}
#ifndef	DEBUG
	clnt_destroy (clnt);  
#endif	 /* DEBUG */
}




/*Verifica que se hayan obtenido correctamente los argumentos
*Asigna el valor a las variables que se utilizan para las operaciones*/
int
main (int argc, char *argv[])
{

//Se definen tanto el host como las variables de entrada
	char *host;
	int a, b;

	if(argc!=4){
		printf("usage: %s server_host num1 num2 \n",argv[0]);
		exit(1);
	}
	host = argv[1];
	if ((a = atoi(argv[2])) == 0 && *argv[2] != '0') {
		fprintf(stderr, "invalid value: %s\n", argv[2]);
		exit(1);
	}
	if ((b = atoi(argv[3])) == 0 && *argv[3] != '0') {
		fprintf(stderr, "invalid value: %s\n", argv[3]);
		exit(1);
	}
	calculadora_prog_1(host, a, b);
}
