import {
  entityTableSelector,
  entityDetailsButtonSelector,
  entityDetailsBackButtonSelector,
  entityCreateButtonSelector,
  entityCreateSaveButtonSelector,
  entityCreateCancelButtonSelector,
  entityEditButtonSelector,
  entityDeleteButtonSelector,
  entityConfirmDeleteButtonSelector,
} from '../../support/entity';

describe('QRCode e2e test', () => {
  const qRCodePageUrl = '/qr-code';
  const qRCodePageUrlPattern = new RegExp('/qr-code(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const qRCodeSample = {
    code: 'mime justly',
    sessionId: '7d0b3cbb-d4b1-4b05-a71e-73f02db4803b',
    athleteId: 'e643e782-a5c4-49c9-b6c3-08fb6be391d9',
    coachId: '94c366b1-0db7-4f81-a992-f9517aadf1de',
  };

  let qRCode;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/qr-codes+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/qr-codes').as('postEntityRequest');
    cy.intercept('DELETE', '/api/qr-codes/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (qRCode) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/qr-codes/${qRCode.id}`,
      }).then(() => {
        qRCode = undefined;
      });
    }
  });

  it('QRCodes menu should load QRCodes page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('qr-code');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('QRCode').should('exist');
    cy.url().should('match', qRCodePageUrlPattern);
  });

  describe('QRCode page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(qRCodePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create QRCode page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/qr-code/new$'));
        cy.getEntityCreateUpdateHeading('QRCode');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', qRCodePageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/qr-codes',
          body: qRCodeSample,
        }).then(({ body }) => {
          qRCode = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/qr-codes+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [qRCode],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(qRCodePageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details QRCode page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('qRCode');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', qRCodePageUrlPattern);
      });

      it('edit button click should load edit QRCode page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('QRCode');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', qRCodePageUrlPattern);
      });

      it('edit button click should load edit QRCode page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('QRCode');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', qRCodePageUrlPattern);
      });

      it('last delete button click should delete instance of QRCode', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('qRCode').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', qRCodePageUrlPattern);

        qRCode = undefined;
      });
    });
  });

  describe('new QRCode page', () => {
    beforeEach(() => {
      cy.visit(`${qRCodePageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('QRCode');
    });

    it('should create an instance of QRCode', () => {
      cy.get(`[data-cy="code"]`).type('partially brr');
      cy.get(`[data-cy="code"]`).should('have.value', 'partially brr');

      cy.get(`[data-cy="sessionId"]`).type('9948b1fd-5543-4f8f-a14b-0aa8453c9a41');
      cy.get(`[data-cy="sessionId"]`).invoke('val').should('match', new RegExp('9948b1fd-5543-4f8f-a14b-0aa8453c9a41'));

      cy.get(`[data-cy="athleteId"]`).type('afd72fda-f730-42d2-9248-32e49a7239f0');
      cy.get(`[data-cy="athleteId"]`).invoke('val').should('match', new RegExp('afd72fda-f730-42d2-9248-32e49a7239f0'));

      cy.get(`[data-cy="coachId"]`).type('6e881228-66d4-46d3-aadd-8d79f4043a2f');
      cy.get(`[data-cy="coachId"]`).invoke('val').should('match', new RegExp('6e881228-66d4-46d3-aadd-8d79f4043a2f'));

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response.statusCode).to.equal(201);
        qRCode = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response.statusCode).to.equal(200);
      });
      cy.url().should('match', qRCodePageUrlPattern);
    });
  });
});
