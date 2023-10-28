/*
 * TEMPLATE
 */


/*Se registran el servicio, se declaran las estructuras y cada función estará alojado en un case para que cuando se haga el request al server
y al entrar a cada case se hace la conveersión de datos a estándar mediante el xdr. 
En el main es donde se lleva a cabo el registro del servicio de y en caso de que no, indica que el registro no se puede*/
#include "calculadora.h"
#include <stdio.h>
#include <stdlib.h>
#include <rpc/pmap_clnt.h>
#include <string.h>
#include <memory.h>
#include <sys/socket.h>
#include <netinet/in.h>

#ifndef SIG_PF
#define SIG_PF void(*)(int)
#endif

static void
calculadora_prog_1(struct svc_req *rqstp, register SVCXPRT *transp)
{
	union {
		dupla_int suma_1_arg;
		dupla_int resta_1_arg;
		dupla_int division_1_arg;
		dupla_int multiplicacion_1_arg;
	} argument;
	char *result;
	xdrproc_t _xdr_argument, _xdr_result;
	char *(*local)(char *, struct svc_req *);

	switch (rqstp->rq_proc) {
	case NULLPROC:
		(void) svc_sendreply (transp, (xdrproc_t) xdr_void, (char *)NULL);
		return;

	case SUMA:
		_xdr_argument = (xdrproc_t) xdr_dupla_int;
		_xdr_result = (xdrproc_t) xdr_int;
		local = (char *(*)(char *, struct svc_req *)) suma_1_svc;
		break;

	case RESTA:
		_xdr_argument = (xdrproc_t) xdr_dupla_int;
		_xdr_result = (xdrproc_t) xdr_int;
		local = (char *(*)(char *, struct svc_req *)) resta_1_svc;
		break;

	case DIVISION:
		_xdr_argument = (xdrproc_t) xdr_dupla_int;
		_xdr_result = (xdrproc_t) xdr_int;
		local = (char *(*)(char *, struct svc_req *)) division_1_svc;
		break;

	case MULTIPLICACION:
		_xdr_argument = (xdrproc_t) xdr_dupla_int;
		_xdr_result = (xdrproc_t) xdr_int;
		local = (char *(*)(char *, struct svc_req *)) multiplicacion_1_svc;
		break;

	default:
		svcerr_noproc (transp);
		return;
	}
	memset ((char *)&argument, 0, sizeof (argument));
	if (!svc_getargs (transp, (xdrproc_t) _xdr_argument, (caddr_t) &argument)) {
		svcerr_decode (transp);
		return;
	}
	result = (*local)((char *)&argument, rqstp);
	if (result != NULL && !svc_sendreply(transp, (xdrproc_t) _xdr_result, result)) {
		svcerr_systemerr (transp);
	}
	if (!svc_freeargs (transp, (xdrproc_t) _xdr_argument, (caddr_t) &argument)) {
		fprintf (stderr, "%s", "unable to free arguments");
		exit (1);
	}
	return;
}

int
main (int argc, char **argv)
{
	register SVCXPRT *transp;

	pmap_unset (CALCULADORA_PROG, CALCULADORA_PROG_VERS);

	transp = svcudp_create(RPC_ANYSOCK);
	if (transp == NULL) {
		fprintf (stderr, "%s", "cannot create udp service.");
		exit(1);
	}
	if (!svc_register(transp, CALCULADORA_PROG, CALCULADORA_PROG_VERS, calculadora_prog_1, IPPROTO_UDP)) {
		fprintf (stderr, "%s", "unable to register (CALCULADORA_PROG, CALCULADORA_PROG_VERS, udp).");
		exit(1);
	}

	transp = svctcp_create(RPC_ANYSOCK, 0, 0);
	if (transp == NULL) {
		fprintf (stderr, "%s", "cannot create tcp service.");
		exit(1);
	}
	if (!svc_register(transp, CALCULADORA_PROG, CALCULADORA_PROG_VERS, calculadora_prog_1, IPPROTO_TCP)) {
		fprintf (stderr, "%s", "unable to register (CALCULADORA_PROG, CALCULADORA_PROG_VERS, tcp).");
		exit(1);
	}

	svc_run ();
	fprintf (stderr, "%s", "svc_run returned");
	exit (1);
	/* NOTREACHED */
}
