/*template*/

/*En este código en todas las funciones se tiene la misma estructura porque
se está haciendo la llamada y conexión con el server solo cambia las funciones correspondientes*/

#include "calculadora.h"

/**dupla_int indica una estructura que contiene las variables de entrada y struct svc_req es 
el para la solicitud que se le hace al servidor*/
int *
suma_1_svc(dupla_int *argp, struct svc_req *rqstp)
{
	static int  result;

	/*
	 * insert server code here
	 */
	printf("\n---El procedimiento SUMA ha sido invocado remotamente---\n");
	printf("Server is working \n");
    printf("parameters: %d, %d\n", argp->a, argp->b);

	printf("Server response to client...\n");
	printf("parameters: %d, %d\n", argp->a, argp->b);
	result = argp->a + argp->b;
	printf("returning: %d\n", result);

	return &result;
}

int *
resta_1_svc(dupla_int *argp, struct svc_req *rqstp)
{
	static int  result;

	/*
	 * insert server code here
	 */
	printf("\n---El procedimiento RESTA ha sido invocado remotamente---\n");
	printf("Server is working \n");
    printf("parameters: %d, %d\n", argp->a, argp->b);
	
	printf("Server response to client...\n");
	printf("parameters: %d, %d\n", argp->a, argp->b);
	result = argp->a - argp->b;
	printf("returning: %d\n", result);
	return &result;
}

int *
division_1_svc(dupla_int *argp, struct svc_req *rqstp)
{
	static int  result;

	/*insert server code here*/
	printf("\n---El procedimiento DIVISION ha sido invocado remotamente---\n");
	printf("Server is working \n");
    printf("parameters: %d, %d\n", argp->a, argp->b);

	printf("Server response to client...\n");
	printf("parameters: %d, %d\n", argp->a, argp->b);
	result = argp->a / argp->b;
	printf("returning: %d\n", result);

	return &result;
}

int *
multiplicacion_1_svc(dupla_int *argp, struct svc_req *rqstp)
{
	static int  result;

	/*insert server code here*/
	printf("\n---El procedimiento MULTIPLICACION ha sido invocado remotamente---\n");
	printf("Server is working \n");
    printf("parameters: %d, %d\n", argp->a, argp->b);

	printf("Server response to client...\n");
	printf("parameters: %d, %d\n", argp->a, argp->b);
	result = argp->a * argp->b;
	printf("returning: %d\n", result);

	return &result;
}
